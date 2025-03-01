package com.company.projects.airBnbApp.service;

import com.company.projects.airBnbApp.dto.BookingDto;
import com.company.projects.airBnbApp.dto.BookingRequest;
import com.company.projects.airBnbApp.dto.HotelReportDto;
import com.company.projects.airBnbApp.entity.enums.BookingStatus;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<Long> guestList);
    String initialisePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    BookingStatus getBookingStatus(Long bookingId);
    List<BookingDto> getAllBookingByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDateTime startDate, LocalDateTime endDate);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getBookings();

}
