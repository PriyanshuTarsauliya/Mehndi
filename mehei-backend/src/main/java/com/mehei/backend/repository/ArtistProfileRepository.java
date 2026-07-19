package com.mehei.backend.repository;

import com.mehei.backend.entity.ArtistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ArtistProfileRepository extends JpaRepository<ArtistProfile, UUID> {
    List<ArtistProfile> findByAvailableTrue();
    List<ArtistProfile> findByIsOnlineTrue();
}
