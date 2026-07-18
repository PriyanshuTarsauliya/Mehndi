package com.mehei.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBookingRequest(
    @NotNull(message = "Artist ID cannot be null")
    UUID artistId,

    @NotNull(message = "Amount cannot be null")
    Double amount
) {}
