package com.mehei.backend.service;

import com.mehei.backend.dto.AuthResponse;
import com.mehei.backend.dto.ProfileSetupRequest;
import com.mehei.backend.entity.ArtistProfile;
import com.mehei.backend.entity.Role;
import com.mehei.backend.entity.User;
import com.mehei.backend.exception.BadRequestException;
import com.mehei.backend.exception.ResourceNotFoundException;
import com.mehei.backend.repository.ArtistProfileRepository;
import com.mehei.backend.repository.UserRepository;
import com.mehei.backend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final ArtistProfileRepository artistProfileRepository;
    private final JwtUtil jwtUtil;
    private final TwilioSmsService twilioSmsService;

    public AuthService(UserRepository userRepository, ArtistProfileRepository artistProfileRepository, JwtUtil jwtUtil, TwilioSmsService twilioSmsService) {
        this.userRepository = userRepository;
        this.artistProfileRepository = artistProfileRepository;
        this.jwtUtil = jwtUtil;
        this.twilioSmsService = twilioSmsService;
    }

    public void sendOtp(String phoneNumber) {
        log.info("OTP_SERVICE: Requesting Twilio to send verification code to {}", phoneNumber);
        twilioSmsService.sendVerificationCode(phoneNumber);
    }

    @Transactional
    public AuthResponse verifyOtp(String phoneNumber, String otpCode) {
        // Verify the code via Twilio
        boolean isValid = twilioSmsService.checkVerificationCode(phoneNumber, otpCode);
        
        if (!isValid) {
            throw new BadRequestException("Invalid or expired OTP code");
        }

        boolean isNewUser = false;
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);

        if (user == null) {
            isNewUser = true;
            // Create a temporary/pending user with empty name and CLIENT role
            user = User.builder()
                    .phoneNumber(phoneNumber)
                    .name("")
                    .email("")
                    .role(Role.CLIENT)
                    .build();
            user = userRepository.save(user);
        } else if (user.getName() == null || user.getName().isBlank()) {
            isNewUser = true;
        }

        String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getId(),
                user.getPhoneNumber(),
                user.getName(),
                user.getRole().name(),
                isNewUser
        );
    }

    @Transactional
    public AuthResponse setupProfile(String phoneNumber, ProfileSetupRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for setup"));

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        final User finalUser = userRepository.save(user);

        // If the user registered as an artist, automatically set up their ArtistProfile
        if (request.role() == Role.ARTIST) {
            ArtistProfile profile = artistProfileRepository.findById(finalUser.getId())
                    .orElseGet(() -> ArtistProfile.builder()
                            .user(finalUser)
                            .category("General")
                            .pricePerHand(500.0) // default pricing
                            .bio("No bio added yet.")
                            .available(true)
                            .build());
            artistProfileRepository.save(profile);
        }

        // Generate a new token with the updated role
        String token = jwtUtil.generateToken(finalUser.getPhoneNumber(), finalUser.getRole().name());

        return new AuthResponse(
                token,
                finalUser.getId(),
                finalUser.getPhoneNumber(),
                finalUser.getName(),
                finalUser.getRole().name(),
                false // no longer a new user after setup
        );
    }
}
