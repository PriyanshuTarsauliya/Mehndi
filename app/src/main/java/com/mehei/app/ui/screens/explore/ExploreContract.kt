package com.mehei.app.ui.screens.explore

import androidx.compose.runtime.Immutable
import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.ArtistTier
import com.mehei.app.domain.model.SortOption

@Immutable
data class ExploreState(
    val artists: List<Artist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCity: String = "Mumbai",
    val filterTier: ArtistTier? = null,
    val showFlashSlotsOnly: Boolean = false,
    val sortBy: SortOption = SortOption.NEAREST,
    val locationDenied: Boolean = false,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val userName: String? = null,
)

sealed interface ExploreEvent {
    data class Search(val query: String) : ExploreEvent
    data class SelectCity(val city: String) : ExploreEvent
    data class FilterByTier(val tier: ArtistTier?) : ExploreEvent
    data object ToggleFlashSlots : ExploreEvent
    data object Refresh : ExploreEvent
    data class ToggleFavorite(val artistId: String) : ExploreEvent
    data object ClearFilters : ExploreEvent
    data class SetSort(val sort: SortOption) : ExploreEvent
    data class UpdateLocation(val lat: Double, val lng: Double) : ExploreEvent
}

sealed interface ExploreEffect {
    data class NavigateToArtist(val artistId: String) : ExploreEffect
    data class ShowSnackbar(val message: String) : ExploreEffect
}
