package com.mehei.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "artist_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistProfile {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "category")
    private String category;

    @Column(name = "price_per_hand")
    private Double pricePerHand;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    // Uber-like Tracking
    @Column(name = "is_online", nullable = false)
    private boolean isOnline = false;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @Column(name = "fcm_token")
    private String fcmToken;
}
