package com.setu.saarthi.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.usecase.GetSavedSchemesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SavedUiState(
    val isLoading: Boolean = true,
    val schemes: List<Scheme> = emptyList()
)

class SavedViewModel(private val getSavedSchemes: GetSavedSchemesUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSavedSchemes().collect { schemes ->
                _uiState.value = SavedUiState(isLoading = false, schemes = schemes)
            }
        }
    }
}
