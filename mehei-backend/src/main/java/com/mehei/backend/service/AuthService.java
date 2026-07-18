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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final ArtistProfileRepository artistProfileRepository;
    private final JwtUtil jwtUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    private record OtpDetails(String code, LocalDateTime expiryTime) {}

    // Temporary storage for OTP codes
    private final Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, ArtistProfileRepository artistProfileRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.artistProfileRepository = artistProfileRepository;
        this.jwtUtil = jwtUtil;
    }

    public void sendOtp(String phoneNumber) {
        // Generate a random 6-digit OTP
        int otpNum = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(otpNum);
        
        // OTP expires in 5 minutes
        OtpDetails details = new OtpDetails(otpCode, LocalDateTime.now().plusMinutes(5));
        otpStorage.put(phoneNumber, details);
        
        log.info("OTP_SERVICE: Generated OTP code {} for phone number {}", otpCode, phoneNumber);
        // TODO: Integrate actual SMS gateway (e.g., Twilio, AWS SNS, etc.) here
    }

    @Transactional
    public AuthResponse verifyOtp(String phoneNumber, String otpCode) {
        OtpDetails storedOtpDetails = otpStorage.get(phoneNumber);
        
        if (storedOtpDetails == null) {
            throw new BadRequestException("Invalid or expired OTP code");
        }
        
        if (LocalDateTime.now().isAfter(storedOtpDetails.expiryTime())) {
            otpStorage.remove(phoneNumber);
            throw new BadRequestException("OTP code has expired");
        }
        
        if (!storedOtpDetails.code().equals(otpCode)) {
            throw new BadRequestException("Invalid OTP code");
        }

        // Clean up OTP after successful verification
        otpStorage.remove(phoneNumber);

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
