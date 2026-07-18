package com.mehei.backend.controller;

import com.mehei.backend.dto.AuthResponse;
import com.mehei.backend.dto.OtpRequest;
import com.mehei.backend.dto.OtpVerificationRequest;
import com.mehei.backend.dto.ProfileSetupRequest;
import com.mehei.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody OtpRequest request) {
        authService.sendOtp(request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        AuthResponse response = authService.verifyOtp(request.phoneNumber(), request.otpCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/setup-profile")
    public ResponseEntity<AuthResponse> setupProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileSetupRequest request) {
        AuthResponse response = authService.setupProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
