package com.mehei.app.domain.repository

import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.ArtistTier
import com.mehei.app.domain.model.FlashSlot
import com.mehei.app.domain.model.Review

interface ArtistRepository {
    suspend fun getArtists(query: String? = null, tier: ArtistTier? = null, flashOnly: Boolean = false): List<Artist>
    suspend fun getArtistById(id: String): Artist?
    suspend fun getReviewsForArtist(artistId: String): List<Review>
    suspend fun getFlashSlotsForArtist(artistId: String, availableOnly: Boolean = true): List<FlashSlot>
}
