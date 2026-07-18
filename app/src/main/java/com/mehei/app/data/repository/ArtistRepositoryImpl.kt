package com.mehei.app.data.repository

import com.mehei.app.data.remote.MeheiApiService
import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.ArtistTier
import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.model.EventType
import com.mehei.app.domain.model.FlashSlot
import com.mehei.app.domain.model.RateCard
import com.mehei.app.domain.model.Review
import com.mehei.app.domain.repository.ArtistRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val apiService: MeheiApiService
) : ArtistRepository {

    override suspend fun getArtists(
        query: String?,
        tier: ArtistTier?,
        flashOnly: Boolean
    ): List<Artist> {
        return try {
            val response = apiService.getArtists()
            if (response.isSuccessful) {
                var artists = response.body()?.map { dto ->
                    Artist(
                        id = dto.id,
                        name = dto.name,
                        rating = 4.8f,
                        experienceYears = 5,
                        bio = dto.bio,
                        city = "Mumbai",
                        profileImageUrl = "",
                        tier = tier ?: ArtistTier.MASTER,
                        specialties = listOf(EventType.MEHNDI_NIGHT, EventType.PARTY),
                        rateCards = listOf(
                            RateCard(Complexity.TRADITIONAL, dto.pricePerHand.toInt(), 500)
                        ),
                        totalReviews = 25,
                        hasFlashSlots = flashOnly
                    )
                } ?: emptyList()

                if (!query.isNullOrBlank()) {
                    artists = artists.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.bio.contains(query, ignoreCase = true)
                    }
                }
                artists
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getArtistById(id: String): Artist? {
        return getArtists().find { it.id == id }
    }

    override suspend fun getReviewsForArtist(artistId: String): List<Review> {
        return emptyList()
    }

    override suspend fun getFlashSlotsForArtist(artistId: String, availableOnly: Boolean): List<FlashSlot> {
        return emptyList()
    }
}
