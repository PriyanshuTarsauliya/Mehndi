package com.mehei.backend.dto;

import java.util.UUID;

public record ArtistProfileResponse(
    UUID id,
    String name,
    String phoneNumber,
    String category,
    Double pricePerHand,
    String bio,
    boolean available
) {}
