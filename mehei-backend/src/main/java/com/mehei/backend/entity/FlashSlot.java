package com.mehei.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "flash_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashSlot {
    @Id
    private String id;
    
    @Column(name = "artist_id", nullable = false)
    private String artistId;
    
    @Column(name = "original_price", nullable = false)
    private Double originalPrice;
    
    @Column(name = "discount_price", nullable = false)
    private Double discountPrice;
    
    @Column(name = "start_time", nullable = false)
    private String startTime;
    
    @Column(name = "end_time", nullable = false)
    private String endTime;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
}
