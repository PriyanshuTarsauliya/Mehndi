package com.mehei.backend.dto;

import jakarta.validation.constraints.NotNull;

public record LocationUpdateDTO(
    @NotNull(message = "Latitude cannot be null")
    Double latitude,

    @NotNull(message = "Longitude cannot be null")
    Double longitude
) {}
