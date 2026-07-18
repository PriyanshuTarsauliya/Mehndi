package com.mehei.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpRequest(
    @NotBlank
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    String phoneNumber
) {}
