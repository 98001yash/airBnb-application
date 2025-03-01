package com.company.projects.airBnbApp.controller;


import com.company.projects.airBnbApp.dto.BookingDto;
import com.company.projects.airBnbApp.dto.GuestDto;
import com.company.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.company.projects.airBnbApp.dto.UserDto;
import com.company.projects.airBnbApp.service.BookingService;
import com.company.projects.airBnbApp.service.GuestService;
import com.company.projects.airBnbApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;
    private final GuestService guestService;

    @PatchMapping("/profile")
    @Operation(summary = "Update the user profile",tags = {"Profile"})
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
        userService.updateProfile(profileUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    @Operation(summary = "Get all my previous bookings ",tags = {"Profile"})
    public ResponseEntity<List<BookingDto>> getMyBookings(){
        return ResponseEntity.ok(bookingService.getBookings());
    }

    @GetMapping("/profile")
    @Operation(summary = "Get all Profile",tags = {"Profile"})
    public ResponseEntity<UserDto> getMyProfile(){
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @GetMapping("/guests")
    @Operation(summary = "Get all my guests",tags = {"Booking Guests"})
    public ResponseEntity<List<GuestDto>> getAllGuests(){
        return ResponseEntity.ok(guestService.getAllGuests());
    }

    @PostMapping("/guests")
    @Operation(summary = "Add a new guest to my guest list",tags = {"Booking Guests"})
    public ResponseEntity<GuestDto> addNewGuest(@RequestBody GuestDto guestDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.addNewGuest(guestDto));
    }

    @PutMapping("guest/{guestId}")
    @Operation(summary = "Update a guest",tags = {"Booking Guests"})
    public ResponseEntity<Void> updateGuest(@PathVariable Long guestId, @RequestBody GuestDto guestDto){
        guestService.updateGuest(guestId,guestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("guests/{guestId}")
    @Operation(summary = "Remove a guest",tags = {"Booking Guests"})
    public ResponseEntity<Void> deleteGuest(@PathVariable Long guestId){
        guestService.deleteGuest(guestId);
        return ResponseEntity.noContent().build();
    }
}
