package com.mehei.app.domain.usecase

import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.ArtistTier
import com.mehei.app.domain.repository.ArtistRepository
import javax.inject.Inject

class GetArtistsUseCase @Inject constructor(
    private val artistRepository: ArtistRepository
) {
    suspend operator fun invoke(
        query: String? = null,
        tier: ArtistTier? = null,
        flashOnly: Boolean = false
    ): List<Artist> {
        return artistRepository.getArtists(query, tier, flashOnly)
    }
}
