package com.setu.saarthi.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setu.saarthi.domain.model.MatchedScheme
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.session.QuerySessionHolder
import com.setu.saarthi.domain.usecase.GetSchemeDetailUseCase
import com.setu.saarthi.domain.usecase.ObserveSavedIdsUseCase
import com.setu.saarthi.domain.usecase.ToggleSavedSchemeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SchemeDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val scheme: Scheme? = null,
    val matched: MatchedScheme? = null,
    val isSaved: Boolean = false
)

class SchemeDetailViewModel(
    private val schemeId: String,
    private val getSchemeDetail: GetSchemeDetailUseCase,
    private val querySessionHolder: QuerySessionHolder,
    private val observeSavedIds: ObserveSavedIdsUseCase,
    private val toggleSavedScheme: ToggleSavedSchemeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchemeDetailUiState())
    val uiState: StateFlow<SchemeDetailUiState> = _uiState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            observeSavedIds().collect { ids ->
                _uiState.update { it.copy(isSaved = schemeId in ids) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getSchemeDetail(schemeId).fold(
                onSuccess = { scheme ->
                    val matched = querySessionHolder.session.value.matchResult?.let { result ->
                        (result.eligibleSchemes + result.partialSchemes).find { it.scheme.schemeId == schemeId }
                    }
                    _uiState.update { it.copy(isLoading = false, scheme = scheme, matched = matched) }
                },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Scheme not found") } }
            )
        }
    }

    fun onRetry() = load()

    fun onToggleSave() {
        viewModelScope.launch { toggleSavedScheme(schemeId) }
    }
}
