package com.company.projects.airBnbApp.service.Impl;

import com.company.projects.airBnbApp.dto.BookingDto;
import com.company.projects.airBnbApp.dto.BookingRequest;
import com.company.projects.airBnbApp.dto.HotelReportDto;
import com.company.projects.airBnbApp.entity.*;
import com.company.projects.airBnbApp.entity.enums.BookingStatus;
import com.company.projects.airBnbApp.exception.ResourceNotFoundException;
import com.company.projects.airBnbApp.exception.UnAuthorisedException;
import com.company.projects.airBnbApp.repository.*;
import com.company.projects.airBnbApp.service.BookingService;
import com.company.projects.airBnbApp.service.CheckoutService;
import com.company.projects.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.company.projects.airBnbApp.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
       log.info("Initialising booking for hotel: {}, room: {}, date {}-{}",bookingRequest.getHotelId(),
               bookingRequest.getRoomId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate());

       Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(()->
               new ResourceNotFoundException("Hotel not found with id: "+bookingRequest.getHotelId()));

       Room room =roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(()->
               new ResourceNotFoundException("Room not found with id:"+ bookingRequest.getRoomId()));

       List<Inventory> inventoryList = inventoryRepository.findByLockAndAvailabilityInventory(room.getId(),
               bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());

       long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;

       if(inventoryList.size()!=daysCount){
           throw new IllegalStateException("Room is not available anymore");
       }


       // reserve the room/update the booked count of inventories
        inventoryRepository.initBooking(room.getId(),bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());

       BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
       BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

       Booking booking = Booking.builder()
               .bookingStatus(BookingStatus.RESERVED)
               .hotel(hotel)
               .room(room)
               .checkInDate(bookingRequest.getCheckInDate())
               .checkOutDate(bookingRequest.getCheckOutDate())
               .user(getCurrentUser())
               .roomsCount(bookingRequest.getRoomsCount())
               .amount(totalPrice)
               .build();

       booking  =bookingRepository.save(booking);
       return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<Long> guestIdList) {
       log.info("Adding guests for booking with id: {}",bookingId);

       Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->
               new ResourceNotFoundException("Booking not found with id: "+bookingId));

       User user = getCurrentUser();
       if(!user.equals(booking.getUser())){
           throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
       }
       if(hasBookingExpired(booking)){
           throw new IllegalStateException("Booking has already expired");
       }
       if(booking.getBookingStatus()!=BookingStatus.RESERVED){
           throw new IllegalStateException("Booking is not under reserved state, cannot add  guests");
       }

       for(Long guestId: guestIdList){
           Guest guest = guestRepository.findById(guestId)
                   .orElseThrow(()->new ResourceNotFoundException("Guest not found with id: "+guestId));
           booking.getGuests().add(guest);
       }
       booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
       booking = bookingRepository.save(booking);
       return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public String initialisePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->
                new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payments" +bookingId +"/status",
                frontendUrl+"/payments/"+bookingId +"/status");

        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
            Session session = (Session)event.getDataObjectDeserializer().getObject().orElse(null);
            if(session==null) return;


            String sessionId = session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(()->
                           new ResourceNotFoundException("Booking not found for session ID: "+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),
                    booking.getCheckOutDate(),booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckInDate(),
                    booking.getCheckOutDate(),booking.getRoomsCount());

            log.info("Successfully confirmed the booking for booking ID: {}",booking.getId());
        }else {
            log.warn("unhandled event type: {}",event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not  belong to this user with id: "+user.getId());
        }
        if(booking.getBookingStatus() !=BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only  confirmed bookings can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),
                booking.getCheckOutDate(),booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getCheckInDate(),
                booking.getCheckOutDate(),booking.getRoomsCount());

        // handle the refund

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParam =  RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParam);
        }catch(StripeException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public BookingStatus getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user  = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }
        return booking.getBookingStatus();
    }

    @Override
    public List<BookingDto> getAllBookingByHotelId(Long hotelId) {
       Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->
               new ResourceNotFoundException("Hotel not found with ID: "+hotelId));
       User user = getCurrentUser();

       log.info("Getting all booking for the hotel with ID: {}",hotelId);
       if(!user.equals(hotel.getOwner())) throw new UnAuthorisedException("You are not the owner of hotel with id: "+hotelId);

       List<Booking> bookings = bookingRepository.findByHotel(hotel);

       return bookings.stream()
               .map((element)->modelMapper.map(element,BookingDto.class))
               .collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
       Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->
               new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

       User user = getCurrentUser();
       log.info("Generating report for hotel with ID: {}",hotelId);
       if(!user.equals(hotel.getOwner())) throw new UnAuthorisedException("You are not the owner of hotel with id: "+hotelId);

       LocalDateTime startDateTime = startDate.atStartOfDay();
       LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

       List<Booking> bookings = bookingRepository.findHotelAndCreatedBetween(hotel,startDateTime,endDateTime);

       Long totalConfirmedBookings = bookings
               .stream()
               .filter(booking->booking.getBookingStatus()==BookingStatus.CONFIRMED)
               .count();

       BigDecimal totalRevenueConfirmedBookings = bookings.stream()
               .filter(booking->booking.getBookingStatus()==BookingStatus.CONFIRMED)
               .map(Booking::getAmount)
               .reduce(BigDecimal.ZERO,BigDecimal::add);

       BigDecimal avgRevenue = totalConfirmedBookings==0? BigDecimal.ZERO:
               totalRevenueConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

       return new HotelReportDto(totalConfirmedBookings,totalRevenueConfirmedBookings,avgRevenue);
    }

    @Override
    public List<BookingDto> getBookings() {
       User user = getCurrentUser();

       return bookingRepository.findByUser(user)
               .stream()
               .map((element)->modelMapper.map(element,BookingDto.class))
               .collect(Collectors.toList());
    }

    public boolean  hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
