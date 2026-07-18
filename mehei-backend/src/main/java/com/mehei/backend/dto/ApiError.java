package com.mehei.backend.dto;

import java.util.List;

public record ApiError(
    String message,
    String type,
    List<String> details
) {
    public static ApiError validation(String message) {
        return new ApiError(message, "validation_error", List.of());
    }

    public static ApiError of(String message) {
        return new ApiError(message, "api_error", List.of());
    }
}
