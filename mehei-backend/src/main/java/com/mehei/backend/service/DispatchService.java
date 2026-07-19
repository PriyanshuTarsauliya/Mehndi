package com.mehei.backend.service;

import com.mehei.backend.dto.BookingRequestPayload;
import com.mehei.backend.entity.ArtistProfile;
import com.mehei.backend.entity.Booking;
import com.mehei.backend.entity.BookingStatus;
import com.mehei.backend.repository.ArtistProfileRepository;
import com.mehei.backend.repository.BookingRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DispatchService {

    private final ArtistProfileRepository artistProfileRepository;
    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Store artists that have been contacted for a specific booking to avoid asking them again
    private final ConcurrentHashMap<UUID, List<UUID>> contactedArtistsMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public DispatchService(ArtistProfileRepository artistProfileRepository, 
                           BookingRepository bookingRepository, 
                           SimpMessagingTemplate messagingTemplate) {
        this.artistProfileRepository = artistProfileRepository;
        this.bookingRepository = bookingRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void dispatchBookingRequest(Booking booking) {
        // Find all online artists
        List<ArtistProfile> onlineArtists = artistProfileRepository.findByIsOnlineTrue();

        if (onlineArtists.isEmpty()) {
            handleNoArtistsAvailable(booking);
            return;
        }

        // Calculate distance and find nearest available artist
        ArtistProfile nearestArtist = findNearestAvailableArtist(booking, onlineArtists);

        if (nearestArtist == null) {
            handleNoArtistsAvailable(booking);
            return;
        }

        // Add to contacted list
        contactedArtistsMap.computeIfAbsent(booking.getId(), k -> new java.util.ArrayList<>()).add(nearestArtist.getId());

        // Create payload
        BookingRequestPayload payload = BookingRequestPayload.builder()
                .bookingId(booking.getId())
                .clientName(booking.getClient().getFullName())
                .clientLat(booking.getClientLatitude())
                .clientLng(booking.getClientLongitude())
                .amount(booking.getAmount())
                .timeoutSeconds(15)
                .build();

        // Send to specific artist
        messagingTemplate.convertAndSendToUser(
                nearestArtist.getUser().getUsername(), 
                "/queue/booking-requests", 
                payload
        );

        // Schedule timeout task
        scheduler.schedule(() -> checkBookingAcceptance(booking.getId(), nearestArtist.getId()), 15, TimeUnit.SECONDS);
    }

    private void checkBookingAcceptance(UUID bookingId, UUID artistId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null && booking.getStatus() == BookingStatus.MATCHING) {
            // The artist didn't accept in time, dispatch to next
            System.out.println("Artist " + artistId + " didn't accept in time. Dispatching to next.");
            dispatchBookingRequest(booking);
        }
    }

    private ArtistProfile findNearestAvailableArtist(Booking booking, List<ArtistProfile> onlineArtists) {
        List<UUID> contacted = contactedArtistsMap.getOrDefault(booking.getId(), List.of());
        
        return onlineArtists.stream()
                .filter(a -> !contacted.contains(a.getId()))
                .filter(a -> a.getCurrentLat() != null && a.getCurrentLng() != null)
                .min((a1, a2) -> {
                    double dist1 = calculateDistance(booking.getClientLatitude(), booking.getClientLongitude(), a1.getCurrentLat(), a1.getCurrentLng());
                    double dist2 = calculateDistance(booking.getClientLatitude(), booking.getClientLongitude(), a2.getCurrentLat(), a2.getCurrentLng());
                    return Double.compare(dist1, dist2);
                })
                .orElse(null);
    }

    private void handleNoArtistsAvailable(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        // Clean up memory
        contactedArtistsMap.remove(booking.getId());
        
        // Notify client
        messagingTemplate.convertAndSendToUser(
                booking.getClient().getUsername(),
                "/queue/booking-updates",
                "NO_ARTISTS_AVAILABLE"
        );
    }

    public void handleArtistAcceptance(UUID bookingId, UUID artistId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        if (booking.getStatus() == BookingStatus.MATCHING) {
            booking.setStatus(BookingStatus.ACCEPTED);
            
            ArtistProfile artistProfile = artistProfileRepository.findById(artistId).orElseThrow();
            booking.setArtist(artistProfile.getUser());
            booking.setArtistLatitude(artistProfile.getCurrentLat());
            booking.setArtistLongitude(artistProfile.getCurrentLng());
            
            bookingRepository.save(booking);
            contactedArtistsMap.remove(bookingId);
            
            // Notify client that artist accepted
            messagingTemplate.convertAndSendToUser(
                    booking.getClient().getUsername(),
                    "/queue/booking-updates",
                    "ARTIST_ACCEPTED:" + artistId
            );
        }
    }

    // Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);
        double dLon = deg2rad(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }
}
