package com.mehei.backend.dto;

import com.mehei.backend.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileSetupRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,

    @Email(message = "Invalid email format")
    String email,

    @NotNull(message = "Role must be specified")
    Role role
) {}
