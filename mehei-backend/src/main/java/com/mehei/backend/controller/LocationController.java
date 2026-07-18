package com.mehei.backend.controller;

import com.mehei.backend.dto.LocationUpdateDTO;
import com.mehei.backend.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateLocation(
            @Valid @RequestBody LocationUpdateDTO locationDTO,
            Principal principal) {
        locationService.updateUserLocation(principal.getName(), locationDTO);
        return ResponseEntity.ok("Location updated successfully");
    }
}
