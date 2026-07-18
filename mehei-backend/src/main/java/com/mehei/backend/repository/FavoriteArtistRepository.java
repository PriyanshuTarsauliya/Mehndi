package com.mehei.backend.repository;

import com.mehei.backend.entity.FavoriteArtist;
import com.mehei.backend.entity.User;
import com.mehei.backend.entity.ArtistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteArtistRepository extends JpaRepository<FavoriteArtist, UUID> {
    List<FavoriteArtist> findByUser(User user);
    Optional<FavoriteArtist> findByUserAndArtistProfile(User user, ArtistProfile artistProfile);
    boolean existsByUserAndArtistProfile(User user, ArtistProfile artistProfile);
}
