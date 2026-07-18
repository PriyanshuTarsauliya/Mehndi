package com.mehei.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    UUID clientId,
    String clientName,
    UUID artistId,
    String artistName,
    Double amount,
    String status,
    LocalDateTime createdAt
) {}
