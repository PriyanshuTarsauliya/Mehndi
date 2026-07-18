package com.mehei.app.data.repository

import com.mehei.app.data.MockData
import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.ArtistTier
import com.mehei.app.domain.model.FlashSlot
import com.mehei.app.domain.model.Review
import com.mehei.app.domain.repository.ArtistRepository
import kotlinx.coroutines.delay

class MockArtistRepositoryImpl : ArtistRepository {

    override suspend fun getArtists(
        query: String?,
        tier: ArtistTier?,
        flashOnly: Boolean
    ): List<Artist> {
        delay(500) // Simulate network delay
        var artists = MockData.artists
        
        if (!query.isNullOrBlank()) {
            artists = artists.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.bio.contains(query, ignoreCase = true)
            }
        }
        
        if (tier != null) {
            artists = artists.filter { it.tier == tier }
        }
        
        if (flashOnly) {
            artists = artists.filter { it.hasFlashSlots }
        }
        
        return artists
    }

    override suspend fun getArtistById(id: String): Artist? {
        delay(300)
        return MockData.artists.find { it.id == id }
    }

    override suspend fun getReviewsForArtist(artistId: String): List<Review> {
        delay(200)
        return MockData.reviews.filter { it.artistId == artistId }
    }

    override suspend fun getFlashSlotsForArtist(artistId: String, availableOnly: Boolean): List<FlashSlot> {
        delay(200)
        return MockData.flashSlots.filter {
            it.artistId == artistId && (!availableOnly || !it.isBooked)
        }
    }
}
