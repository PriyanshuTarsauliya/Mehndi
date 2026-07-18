package com.mehei.backend.controller;

import com.mehei.backend.dto.BookingResponse;
import com.mehei.backend.dto.CreateBookingRequest;
import com.mehei.backend.security.UserDetailsImpl;
import com.mehei.backend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/create")
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID userId) {
        
        // Security check: users can only view their own booking history
        if (!userDetails.getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to view this booking history");
        }
        
        List<BookingResponse> bookings = bookingService.getBookingHistory(userId);
        return ResponseEntity.ok(bookings);
    }
}
