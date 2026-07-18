package com.mehei.app.ui.screens.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(RequestState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<RequestEffect>()
    val effects = _effects.asSharedFlow()

    init {
        recalculateEstimate()
    }

    fun onEvent(event: RequestEvent) {
        when (event) {
            is RequestEvent.OnComplexityChanged -> {
                _state.update { it.copy(selectedComplexity = event.complexity) }
                recalculateEstimate()
            }
            is RequestEvent.OnEventTypeChanged -> {
                _state.update { it.copy(selectedEventType = event.eventType) }
                recalculateEstimate()
            }
            is RequestEvent.OnNumHandsChanged -> {
                _state.update { it.copy(selectedNumHands = event.numHands) }
                recalculateEstimate()
            }
            RequestEvent.OnRequestArtistClick -> requestArtist()
        }
    }

    private fun recalculateEstimate() {
        // Dummy calculation for demonstration
        val basePrice = 500
        val handsMultiplier = _state.value.selectedNumHands * 150
        val complexityMultiplier = when (_state.value.selectedComplexity.name) {
            "LOW" -> 1.0
            "MEDIUM" -> 1.5
            "HIGH" -> 2.0
            else -> 1.0
        }
        val estimatedTotal = ((basePrice + handsMultiplier) * complexityMultiplier).toInt()
        
        _state.update { it.copy(estimatedPrice = estimatedTotal) }
    }

    private fun requestArtist() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // Simulate network call
            kotlinx.coroutines.delay(500)
            _state.update { it.copy(isLoading = false) }
            val requestId = UUID.randomUUID().toString()
            _effects.emit(RequestEffect.NavigateToMatching(requestId))
        }
    }
}
