package com.mehei.backend.service;

import com.mehei.backend.dto.BookingResponse;
import com.mehei.backend.dto.CreateBookingRequest;
import com.mehei.backend.entity.Booking;
import com.mehei.backend.entity.BookingStatus;
import com.mehei.backend.entity.Role;
import com.mehei.backend.entity.User;
import com.mehei.backend.exception.BadRequestException;
import com.mehei.backend.exception.ResourceNotFoundException;
import com.mehei.backend.repository.BookingRepository;
import com.mehei.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse createBooking(String clientPhoneNumber, CreateBookingRequest request) {
        User client = userRepository.findByPhoneNumber(clientPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        User artist = userRepository.findById(request.artistId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found"));

        if (artist.getRole() != Role.ARTIST) {
            throw new BadRequestException("Selected user is not registered as an artist");
        }

        Booking booking = Booking.builder()
                .client(client)
                .artist(artist)
                .status(BookingStatus.REQUESTED)
                .amount(request.amount())
                .createdAt(LocalDateTime.now())
                .build();

        booking = bookingRepository.save(booking);

        return mapToResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingHistory(UUID userId) {
        List<Booking> bookings = bookingRepository.findByClientIdOrArtistIdOrderByCreatedAtDesc(userId, userId);
        return bookings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse mapToResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getClient().getId(),
                booking.getClient().getName(),
                booking.getArtist().getId(),
                booking.getArtist().getName(),
                booking.getAmount(),
                booking.getStatus().name(),
                booking.getCreatedAt()
        );
    }
}
