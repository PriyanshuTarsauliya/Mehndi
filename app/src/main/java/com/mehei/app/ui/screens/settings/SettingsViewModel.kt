package com.mehei.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsState(
    val email: String = "user@example.com",
    val isDarkMode: Boolean = false,
    val language: String = "English",
    val notificationsEnabled: Boolean = true,
    val appVersion: String = "1.0.0"
)

sealed class SettingsEvent {
    data class OnEmailChange(val email: String) : SettingsEvent()
    data class OnDarkModeToggle(val isDarkMode: Boolean) : SettingsEvent()
    data class OnLanguageChange(val language: String) : SettingsEvent()
    data class OnNotificationsToggle(val enabled: Boolean) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }
            is SettingsEvent.OnDarkModeToggle -> {
                _state.update { it.copy(isDarkMode = event.isDarkMode) }
                // TODO: Persist via DataStore and update App Theme dynamically
            }
            is SettingsEvent.OnLanguageChange -> {
                _state.update { it.copy(language = event.language) }
            }
            is SettingsEvent.OnNotificationsToggle -> {
                _state.update { it.copy(notificationsEnabled = event.enabled) }
            }
        }
    }
}
