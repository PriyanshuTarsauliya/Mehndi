package com.mehei.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestPayload {
    private UUID bookingId;
    private String clientName;
    private Double clientLat;
    private Double clientLng;
    private String category;
    private Double amount;
    private Integer timeoutSeconds;
}
