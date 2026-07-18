package com.mehei.app.ui.screens.artistdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.FlashSlot
import com.mehei.app.domain.model.Review
import com.mehei.app.domain.repository.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mehei.app.domain.repository.FavoritesRepository

import com.mehei.app.domain.usecase.GetFlashSlotsUseCase

data class ArtistDetailState(
    val artist: Artist? = null,
    val reviews: List<Review> = emptyList(),
    val flashSlots: List<FlashSlot> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val artistRepository: ArtistRepository,
    private val getFlashSlotsUseCase: GetFlashSlotsUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val artistId: String = checkNotNull(savedStateHandle["artistId"])

    private val _state = MutableStateFlow(ArtistDetailState())
    val state: StateFlow<ArtistDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect {
                loadArtistDetails()
            }
        }
    }

    private fun loadArtistDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val artist = artistRepository.getArtistById(artistId)
            val artistWithFavorite = artist?.copy(isFavorite = favoritesRepository.isFavorite(artistId))
            val reviews = artistRepository.getReviewsForArtist(artistId)
            val flashSlots = if (artist?.hasFlashSlots == true) {
                getFlashSlotsUseCase(artistId)
            } else {
                emptyList()
            }

            _state.update {
                it.copy(
                    artist = artistWithFavorite,
                    reviews = reviews,
                    flashSlots = flashSlots,
                    isLoading = false,
                )
            }
        }
    }

    fun toggleFavorite() {
        favoritesRepository.toggleFavorite(artistId)
    }
}
