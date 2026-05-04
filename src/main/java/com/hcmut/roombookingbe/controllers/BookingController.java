package com.hcmut.roombookingbe.controllers;

import com.hcmut.roombookingbe.dtos.BookingDTO;
import com.hcmut.roombookingbe.dtos.CreateBookingRequestDTO;
import com.hcmut.roombookingbe.dtos.request.ReviewBookingRequest;
import com.hcmut.roombookingbe.entities.User;
import com.hcmut.roombookingbe.enums.BookingStatus;
import com.hcmut.roombookingbe.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDTO createBooking(@Valid @RequestBody CreateBookingRequestDTO request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return bookingService.createBooking(request, currentUser);
    }

    @GetMapping
    public List<BookingDTO> getBookings(
            @RequestParam(required = false) BookingStatus status,
            Authentication authentication) {
        return bookingService.getBookings(status);
    }

    @PutMapping("/{id}/approve")
    public BookingDTO approveBooking(@PathVariable Long id, Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        return bookingService.approveBooking(id, admin);
    }

    @PutMapping("/{id}/reject")
    public BookingDTO rejectBooking(
            @PathVariable Long id,
            @RequestBody ReviewBookingRequest request,
            Authentication authentication) {
        User admin = (User) authentication.getPrincipal();
        return bookingService.rejectBooking(id, admin, request.rejectReason());
    }

    @GetMapping("/history")
    public List<BookingDTO> getBookingHistory(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return bookingService.getBookingsByUser(currentUser.getId());
    }
}
