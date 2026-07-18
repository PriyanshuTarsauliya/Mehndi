package com.mehei.backend.repository;

import com.mehei.backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"client", "artist"})
    List<Booking> findByClientIdOrArtistIdOrderByCreatedAtDesc(UUID clientId, UUID artistId);
}
