package com.mehei.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

import com.mehei.app.data.local.TokenManager

data class ProfileState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val initial: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        val name = tokenManager.getUserName() ?: "Guest"
        val phone = tokenManager.getUserPhone() ?: ""
        val initial = if (name.isNotEmpty()) name.take(1).uppercase() else "G"
        _state.value = ProfileState(
            name = name,
            phone = phone,
            email = "",
            initial = initial
        )
    }
}
