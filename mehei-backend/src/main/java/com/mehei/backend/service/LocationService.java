package com.mehei.backend.service;

import com.mehei.backend.dto.LocationUpdateDTO;
import com.mehei.backend.entity.Location;
import com.mehei.backend.entity.User;
import com.mehei.backend.exception.ResourceNotFoundException;
import com.mehei.backend.repository.LocationRepository;
import com.mehei.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public LocationService(LocationRepository locationRepository, UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateUserLocation(String phoneNumber, LocationUpdateDTO request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for location update"));

        Location location = locationRepository.findById(user.getId())
                .orElseGet(() -> Location.builder()
                        .user(user)
                        .latitude(request.latitude())
                        .longitude(request.longitude())
                        .updatedAt(LocalDateTime.now())
                        .build());

        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setUpdatedAt(LocalDateTime.now());

        locationRepository.save(location);
    }
}
