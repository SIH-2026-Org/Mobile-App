package com.setu.saarthi.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setu.saarthi.domain.model.UserProfile
import com.setu.saarthi.domain.usecase.ObserveProfileUseCase
import com.setu.saarthi.domain.usecase.SaveProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile = UserProfile(),
    val isSaving: Boolean = false,
    val savedJustNow: Boolean = false
)

class ProfileViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = observeProfile().first()
            _uiState.update { it.copy(isLoading = false, profile = profile) }
        }
    }

    fun onFieldChange(profile: UserProfile) {
        _uiState.update { it.copy(profile = profile, savedJustNow = false) }
    }

    fun onSave() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val toSave = _uiState.value.profile.copy(onboardingComplete = true)
            saveProfile(toSave)
            _uiState.update { it.copy(isSaving = false, profile = toSave, savedJustNow = true) }
        }
    }
}
