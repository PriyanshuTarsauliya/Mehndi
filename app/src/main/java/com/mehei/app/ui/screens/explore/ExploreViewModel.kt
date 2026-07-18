package com.mehei.app.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehei.app.domain.model.SortOption
import com.mehei.app.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mehei.app.domain.usecase.GetArtistsUseCase
import com.mehei.app.domain.repository.FavoritesRepository

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getArtistsUseCase: GetArtistsUseCase,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreState())
    val state: StateFlow<ExploreState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ExploreEffect>()
    val effects: SharedFlow<ExploreEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect {
                loadArtists()
            }
        }
    }

    fun onEvent(event: ExploreEvent) {
        when (event) {
            is ExploreEvent.Search -> {
                _state.update { it.copy(searchQuery = event.query) }
                loadArtists()
            }
            is ExploreEvent.SelectCity -> {
                _state.update { it.copy(selectedCity = event.city) }
                loadArtists()
            }
            is ExploreEvent.FilterByTier -> {
                _state.update { it.copy(filterTier = event.tier) }
                loadArtists()
            }
            is ExploreEvent.ToggleFlashSlots -> {
                _state.update { it.copy(showFlashSlotsOnly = !it.showFlashSlotsOnly) }
                loadArtists()
            }
            is ExploreEvent.Refresh -> loadArtists()
            is ExploreEvent.ToggleFavorite -> {
                favoritesRepository.toggleFavorite(event.artistId)
            }
            is ExploreEvent.ClearFilters -> {
                _state.update { 
                    it.copy(
                        searchQuery = "",
                        filterTier = null,
                        showFlashSlotsOnly = false,
                        sortBy = SortOption.NEAREST
                    ) 
                }
                loadArtists()
            }
            is ExploreEvent.SetSort -> {
                _state.update { it.copy(sortBy = event.sort) }
                loadArtists()
            }
            is ExploreEvent.UpdateLocation -> {
                _state.update { it.copy(userLat = event.lat, userLng = event.lng) }
                loadArtists()
            }
        }
    }

    fun onArtistClick(artistId: String) {
        viewModelScope.launch {
            _effects.emit(ExploreEffect.NavigateToArtist(artistId))
        }
    }

    private fun loadArtists() {
        val current = _state.value
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val artists = getArtistsUseCase(
                query = current.searchQuery,
                tier = current.filterTier,
                flashOnly = current.showFlashSlotsOnly
            )

            // Filter by city, mark favorites, and compute distances
            val userLat = current.userLat
            val userLng = current.userLng

            val withDistances = artists.filter { 
                it.city.equals(current.selectedCity, ignoreCase = true) 
            }.map { artist ->
                val distance = if (userLat != null && userLng != null &&
                    artist.latitude != 0.0 && artist.longitude != 0.0
                ) {
                    LocationHelper.distanceKm(
                        userLat, userLng,
                        artist.latitude, artist.longitude
                    )
                } else {
                    null
                }
                artist.copy(
                    isFavorite = favoritesRepository.isFavorite(artist.id),
                    distanceKm = distance
                )
            }
            
            // Apply sort
            val sorted = when (current.sortBy) {
                SortOption.NEAREST -> withDistances.sortedBy { it.distanceKm ?: Float.MAX_VALUE }
                SortOption.RATING -> withDistances.sortedByDescending { it.rating }
                SortOption.PRICE_LOW -> withDistances.sortedBy { 
                    it.rateCards.minOfOrNull { rc -> rc.pricePerHand } ?: 0 
                }
                SortOption.PRICE_HIGH -> withDistances.sortedByDescending { 
                    it.rateCards.maxOfOrNull { rc -> rc.pricePerHand } ?: 0 
                }
            }
            
            _state.update {
                it.copy(
                    artists = sorted,
                    isLoading = false,
                )
            }
        }
    }
}
