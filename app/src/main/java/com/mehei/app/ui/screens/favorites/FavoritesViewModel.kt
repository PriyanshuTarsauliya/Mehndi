package com.mehei.app.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.repository.ArtistRepository
import com.mehei.app.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesState(
    val favoriteArtists: List<Artist> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val artistRepository: ArtistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect { ids ->
                loadFavorites(ids)
            }
        }
    }

    private suspend fun loadFavorites(ids: Set<String>) {
        _state.update { it.copy(isLoading = true) }
        val artists = artistRepository.getArtists(null, null, false)
        val favorites = artists.filter { ids.contains(it.id) }.map { it.copy(isFavorite = true) }
        
        _state.update {
            it.copy(
                favoriteArtists = favorites,
                isLoading = false
            )
        }
    }

    fun removeFavorite(artistId: String) {
        favoritesRepository.toggleFavorite(artistId)
    }
}
