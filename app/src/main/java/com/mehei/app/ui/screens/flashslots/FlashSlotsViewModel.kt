package com.mehei.app.ui.screens.flashslots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehei.app.domain.model.Artist
import com.mehei.app.domain.model.FlashSlot
import com.mehei.app.domain.usecase.GetArtistsUseCase
import com.mehei.app.domain.usecase.GetFlashSlotsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlashSlotsState(
    val isLoading: Boolean = true,
    val artistsWithSlots: Map<Artist, List<FlashSlot>> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class FlashSlotsViewModel @Inject constructor(
    private val getArtistsUseCase: GetArtistsUseCase,
    private val getFlashSlotsUseCase: GetFlashSlotsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FlashSlotsState())
    val state: StateFlow<FlashSlotsState> = _state.asStateFlow()

    init {
        loadFlashSlots()
    }

    fun loadFlashSlots() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Get artists that have flash slots
                val artists = getArtistsUseCase(flashOnly = true)
                val map = mutableMapOf<Artist, List<FlashSlot>>()
                
                // For each artist, get their available flash slots
                for (artist in artists) {
                    val slots = getFlashSlotsUseCase(artist.id, availableOnly = true)
                    if (slots.isNotEmpty()) {
                        map[artist] = slots
                    }
                }
                
                _state.update { it.copy(isLoading = false, artistsWithSlots = map) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Failed to load flash deals") }
            }
        }
    }
}
