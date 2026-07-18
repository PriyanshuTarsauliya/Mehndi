package com.mehei.app.domain.repository

import com.mehei.app.data.remote.MeheiApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for storing favorite artists using the backend API.
 */
@Singleton
class FavoritesRepository @Inject constructor(
    private val apiService: MeheiApiService
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        fetchFavorites()
    }

    private fun fetchFavorites() {
        scope.launch {
            try {
                val response = apiService.getFavorites()
                if (response.isSuccessful) {
                    response.body()?.let { favorites ->
                        _favoriteIds.value = favorites.map { it.id }.toSet()
                    }
                }
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun toggleFavorite(artistId: String) {
        val isAdding = !_favoriteIds.value.contains(artistId)

        // Optimistic UI update
        _favoriteIds.update { current ->
            if (current.contains(artistId)) {
                current - artistId
            } else {
                current + artistId
            }
        }

        scope.launch {
            try {
                if (isAdding) {
                    apiService.addFavorite(artistId)
                } else {
                    apiService.removeFavorite(artistId)
                }
            } catch (e: Exception) {
                // Revert on failure
                _favoriteIds.update { current ->
                    if (isAdding) current - artistId else current + artistId
                }
            }
        }
    }

    fun isFavorite(artistId: String): Boolean {
        return _favoriteIds.value.contains(artistId)
    }
}
