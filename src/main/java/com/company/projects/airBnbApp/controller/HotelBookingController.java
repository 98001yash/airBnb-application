package com.company.projects.airBnbApp.controller;


import com.company.projects.airBnbApp.dto.BookingDto;
import com.company.projects.airBnbApp.dto.BookingPaymentInitResponseDto;
import com.company.projects.airBnbApp.dto.BookingRequest;
import com.company.projects.airBnbApp.dto.BookingStatusResponseDto;
import com.company.projects.airBnbApp.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    @Operation(summary = "Initiate the booking",tags = {"Booking Flow"})
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequest bookingRequest){
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }

    @PostMapping("/{bookingId}/addGuests")
    @Operation(summary = "Add guest ids to the bookings",tags = {"Booking Flow"})
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId,
                                                @RequestBody List<Long> guestIdList){
        return ResponseEntity.ok(bookingService.addGuests(bookingId, guestIdList));
    }


    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel the booking", tags = {"Booking Flow}"})
    public ResponseEntity<BookingPaymentInitResponseDto> initiatePayment(@PathVariable Long bookingId){
        String sessionUrl = bookingService.initialisePayments(bookingId);
        return ResponseEntity.ok(new BookingPaymentInitResponseDto(sessionUrl));
    }

    @GetMapping("/{bookingId}/status")
    @Operation(summary = "Check the status of the booking",tags = {"Booking Flow"})
    public ResponseEntity<BookingStatusResponseDto> getBookingStatus(@PathVariable Long bookingId){
        return ResponseEntity.ok(new BookingStatusResponseDto(bookingService.getBookingStatus(bookingId)));
    }

}
