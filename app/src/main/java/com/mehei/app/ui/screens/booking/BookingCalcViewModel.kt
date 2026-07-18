package com.mehei.app.ui.screens.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.repository.ArtistRepository
import com.mehei.app.domain.usecase.CalculatePriceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingCalcViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val artistRepository: ArtistRepository,
    private val calculatePrice: CalculatePriceUseCase,
) : ViewModel() {

    private val artistId: String = checkNotNull(savedStateHandle["artistId"])

    private val _state = MutableStateFlow(BookingCalcState())
    val state: StateFlow<BookingCalcState> = _state.asStateFlow()

    private val _effects = kotlinx.coroutines.flow.MutableSharedFlow<BookingCalcEffect>()
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            val artist = artistRepository.getArtistById(artistId)
            _state.update { it.copy(artist = artist) }
            recalculate()
        }
    }

    fun onEvent(event: BookingCalcEvent) {
        when (event) {
            is BookingCalcEvent.SetHands -> {
                _state.update { it.copy(numHands = event.count.coerceIn(1, 40)) }
                recalculate()
            }
            is BookingCalcEvent.SetComplexity -> {
                _state.update { it.copy(selectedComplexity = event.complexity) }
                recalculate()
            }
            is BookingCalcEvent.SetHours -> {
                _state.update { it.copy(estimatedHours = event.hours.coerceIn(0.5f, 8f)) }
                recalculate()
            }
            is BookingCalcEvent.SetEventType -> {
                _state.update { it.copy(selectedEventType = event.type) }
            }
            is BookingCalcEvent.ToggleOrganicHenna -> {
                _state.update {
                    it.copy(
                        includeOrganic = event.include,
                        materialFee = if (event.include) 200 else 0,
                    )
                }
                recalculate()
            }
            is BookingCalcEvent.SetNote -> {
                _state.update { it.copy(customerNote = event.note) }
            }
            is BookingCalcEvent.ConfirmBooking -> {
                val currentState = _state.value
                val artistId = currentState.artist?.id ?: return
                val deposit = currentState.priceEstimate?.deposit ?: return
                viewModelScope.launch {
                    _effects.emit(BookingCalcEffect.NavigateToCheckout(artistId, deposit))
                }
            }
        }
    }

    private fun recalculate() {
        val current = _state.value
        val artist = current.artist ?: return
        val rateCard = artist.rateCards.find { it.complexity == current.selectedComplexity }
            ?: return

        val estimate = calculatePrice(
            rateCard = rateCard,
            numHands = current.numHands,
            estimatedHours = current.estimatedHours,
            flashSlotDiscountPercent = current.flashSlotDiscountPercent,
            materialFee = current.materialFee,
        )
        _state.update { it.copy(priceEstimate = estimate) }
    }
}
