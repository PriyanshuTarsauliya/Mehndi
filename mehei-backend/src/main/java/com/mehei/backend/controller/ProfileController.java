package com.mehei.backend.controller;

import com.mehei.backend.entity.User;
import com.mehei.backend.exception.ResourceNotFoundException;
import com.mehei.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final String uploadDir = "uploads/profiles/";

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
        // Ensure upload directory exists
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!");
        }
    }

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            // Generate a unique file name
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);

            // Save the file locally
            Files.write(filePath, file.getBytes());

            // Construct the public URL to access the image
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/profiles/")
                    .path(fileName)
                    .toUriString();

            // Update user in DB
            User user = userRepository.findByPhoneNumber(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            user.setProfileImageUrl(fileDownloadUri);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("profileImageUrl", fileDownloadUri));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not upload the file: " + e.getMessage()));
        }
    }
}
