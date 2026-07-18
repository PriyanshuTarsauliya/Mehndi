package com.mehei.backend.service;

import com.mehei.backend.dto.ArtistProfileResponse;
import com.mehei.backend.entity.ArtistProfile;
import com.mehei.backend.repository.ArtistProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArtistService {

    private final ArtistProfileRepository artistProfileRepository;

    public ArtistService(ArtistProfileRepository artistProfileRepository) {
        this.artistProfileRepository = artistProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<ArtistProfileResponse> getAvailableArtists() {
        List<ArtistProfile> profiles = artistProfileRepository.findByAvailableTrue();
        return profiles.stream()
                .map(profile -> new ArtistProfileResponse(
                        profile.getId(),
                        profile.getUser().getName(),
                        profile.getUser().getPhoneNumber(),
                        profile.getCategory(),
                        profile.getPricePerHand(),
                        profile.getBio(),
                        profile.isAvailable()
                ))
                .collect(Collectors.toList());
    }
}
