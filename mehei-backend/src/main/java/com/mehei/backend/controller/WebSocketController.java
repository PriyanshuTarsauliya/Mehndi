package com.mehei.backend.controller;

import com.mehei.backend.dto.LocationUpdatePayload;
import com.mehei.backend.entity.ArtistProfile;
import com.mehei.backend.entity.Booking;
import com.mehei.backend.entity.BookingStatus;
import com.mehei.backend.repository.ArtistProfileRepository;
import com.mehei.backend.repository.BookingRepository;
import com.mehei.backend.service.DispatchService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Controller
@RestController
public class WebSocketController {

    private final ArtistProfileRepository artistProfileRepository;
    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final DispatchService dispatchService;

    public WebSocketController(ArtistProfileRepository artistProfileRepository,
                               BookingRepository bookingRepository,
                               SimpMessagingTemplate messagingTemplate,
                               DispatchService dispatchService) {
        this.artistProfileRepository = artistProfileRepository;
        this.bookingRepository = bookingRepository;
        this.messagingTemplate = messagingTemplate;
        this.dispatchService = dispatchService;
    }

    // Artist sends location updates to /app/location
    @MessageMapping("/location")
    public void handleLocationUpdate(@Payload LocationUpdatePayload payload, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
        if (username != null) {
            ArtistProfile profile = artistProfileRepository.findById(payload.getArtistId()).orElse(null);
            if (profile != null) {
                profile.setCurrentLat(payload.getLat());
                profile.setCurrentLng(payload.getLng());
                artistProfileRepository.save(profile);
                
                // If artist is on the way, we might want to forward this to the specific client
                // Find active bookings for this artist
                // For MVP, we broadcast to a topic based on artist ID that the client can subscribe to
                messagingTemplate.convertAndSend("/topic/artist-location/" + profile.getId(), payload);
            }
        }
    }

    // Client requests a booking via REST (could also be via WS)
    @PostMapping("/api/bookings/request")
    public Booking requestBooking(Double lat, Double lng, Double amount, String username) {
        // Find user by username, etc. (Skipped detailed auth for brevity)
        // Assume we have a valid Booking object here
        Booking booking = new Booking();
        booking.setClientLatitude(lat);
        booking.setClientLongitude(lng);
        booking.setAmount(amount);
        booking.setStatus(BookingStatus.MATCHING);
        // ... (Set client)
        
        booking = bookingRepository.save(booking);
        dispatchService.dispatchBookingRequest(booking);
        return booking;
    }

    // Artist accepts booking
    @PostMapping("/api/bookings/{bookingId}/accept")
    public void acceptBooking(@PathVariable UUID bookingId, UUID artistId) {
        dispatchService.handleArtistAcceptance(bookingId, artistId);
    }
}
