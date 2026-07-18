package com.mehei.backend.repository;

import com.mehei.backend.entity.FlashSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashSlotRepository extends JpaRepository<FlashSlot, String> {
    List<FlashSlot> findByArtistId(String artistId);
    List<FlashSlot> findByArtistIdAndIsAvailableTrue(String artistId);
    List<FlashSlot> findByIsAvailableTrue();
}
