package com.mehei.backend.controller;

import com.mehei.backend.dto.ArtistProfileResponse;
import com.mehei.backend.service.ArtistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping
    public ResponseEntity<List<ArtistProfileResponse>> getAvailableArtists() {
        List<ArtistProfileResponse> artists = artistService.getAvailableArtists();
        return ResponseEntity.ok(artists);
    }
}
