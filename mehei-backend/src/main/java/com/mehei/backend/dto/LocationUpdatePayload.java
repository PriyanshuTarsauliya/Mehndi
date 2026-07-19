package com.mehei.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdatePayload {
    private UUID artistId;
    private Double lat;
    private Double lng;
}
