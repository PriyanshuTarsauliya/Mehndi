package com.mehei.backend.service;

import com.mehei.backend.dto.ArtistProfileResponse;
import com.mehei.backend.entity.ArtistProfile;
import com.mehei.backend.entity.FavoriteArtist;
import com.mehei.backend.entity.User;
import com.mehei.backend.repository.ArtistProfileRepository;
import com.mehei.backend.repository.FavoriteArtistRepository;
import com.mehei.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FavoritesService {

    private final FavoriteArtistRepository favoriteArtistRepository;
    private final UserRepository userRepository;
    private final ArtistProfileRepository artistProfileRepository;

    public FavoritesService(FavoriteArtistRepository favoriteArtistRepository,
                            UserRepository userRepository,
                            ArtistProfileRepository artistProfileRepository) {
        this.favoriteArtistRepository = favoriteArtistRepository;
        this.userRepository = userRepository;
        this.artistProfileRepository = artistProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<ArtistProfileResponse> getFavoriteArtists(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FavoriteArtist> favorites = favoriteArtistRepository.findByUser(user);

        return favorites.stream()
                .map(fav -> {
                    ArtistProfile profile = fav.getArtistProfile();
                    User artistUser = profile.getUser();
                    return new ArtistProfileResponse(
                            profile.getId(),
                            artistUser.getName(),
                            artistUser.getPhoneNumber(),
                            profile.getCategory(),
                            profile.getPricePerHand(),
                            profile.getBio(),
                            profile.isAvailable()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavorite(String phoneNumber, UUID artistId) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ArtistProfile artistProfile = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        if (!favoriteArtistRepository.existsByUserAndArtistProfile(user, artistProfile)) {
            FavoriteArtist favoriteArtist = FavoriteArtist.builder()
                    .user(user)
                    .artistProfile(artistProfile)
                    .build();
            favoriteArtistRepository.save(favoriteArtist);
        }
    }

    @Transactional
    public void removeFavorite(String phoneNumber, UUID artistId) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ArtistProfile artistProfile = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        favoriteArtistRepository.findByUserAndArtistProfile(user, artistProfile)
                .ifPresent(favoriteArtistRepository::delete);
    }
}
