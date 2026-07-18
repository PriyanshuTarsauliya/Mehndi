package com.mehei.app.domain.usecase

import com.mehei.app.domain.model.FlashSlot
import com.mehei.app.domain.repository.ArtistRepository
import javax.inject.Inject

class GetFlashSlotsUseCase @Inject constructor(
    private val artistRepository: ArtistRepository
) {
    suspend operator fun invoke(
        artistId: String,
        availableOnly: Boolean = true
    ): List<FlashSlot> {
        return artistRepository.getFlashSlotsForArtist(artistId, availableOnly)
    }
}
