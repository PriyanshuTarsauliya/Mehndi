package com.mehei.backend.controller;

import com.mehei.backend.dto.ArtistProfileResponse;
import com.mehei.backend.security.UserDetailsImpl;
import com.mehei.backend.service.FavoritesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {

    private final FavoritesService favoritesService;

    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @GetMapping
    public ResponseEntity<List<ArtistProfileResponse>> getFavorites(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ArtistProfileResponse> favorites = favoritesService.getFavoriteArtists(userDetails.getUsername());
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{artistId}")
    public ResponseEntity<Void> addFavorite(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID artistId) {
        favoritesService.addFavorite(userDetails.getUsername(), artistId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{artistId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID artistId) {
        favoritesService.removeFavorite(userDetails.getUsername(), artistId);
        return ResponseEntity.ok().build();
    }
}
