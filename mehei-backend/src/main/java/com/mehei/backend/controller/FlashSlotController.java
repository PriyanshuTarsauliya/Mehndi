package com.mehei.backend.controller;

import com.mehei.backend.entity.FlashSlot;
import com.mehei.backend.repository.FlashSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/flash-slots")
public class FlashSlotController {

    @Autowired
    private FlashSlotRepository flashSlotRepository;

    @GetMapping("/available")
    public ResponseEntity<List<FlashSlot>> getAllAvailableFlashSlots() {
        return ResponseEntity.ok(flashSlotRepository.findByIsAvailableTrue());
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<FlashSlot>> getFlashSlotsForArtist(@PathVariable String artistId, @RequestParam(defaultValue = "true") boolean availableOnly) {
        if (availableOnly) {
            return ResponseEntity.ok(flashSlotRepository.findByArtistIdAndIsAvailableTrue(artistId));
        } else {
            return ResponseEntity.ok(flashSlotRepository.findByArtistId(artistId));
        }
    }

    @PostMapping
    public ResponseEntity<FlashSlot> createFlashSlot(@RequestBody FlashSlot flashSlot) {
        if (flashSlot.getId() == null || flashSlot.getId().isEmpty()) {
            flashSlot.setId(UUID.randomUUID().toString());
        }
        FlashSlot saved = flashSlotRepository.save(flashSlot);
        return ResponseEntity.ok(saved);
    }
}
