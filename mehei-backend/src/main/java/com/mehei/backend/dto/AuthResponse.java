package com.mehei.backend.dto;

import java.util.UUID;

public record AuthResponse(
    String token,
    UUID userId,
    String phoneNumber,
    String name,
    String role,
    boolean isNewUser
) {}
