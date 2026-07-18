package com.mehei.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerificationRequest(
    @NotBlank
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    String phoneNumber,

    @NotBlank
    @Size(min = 6, max = 6, message = "OTP code must be exactly 6 digits")
    String otpCode
) {}
