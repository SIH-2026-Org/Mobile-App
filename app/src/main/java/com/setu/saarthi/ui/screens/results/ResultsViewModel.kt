package com.setu.saarthi.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setu.saarthi.domain.model.MatchResult
import com.setu.saarthi.domain.session.QuerySessionHolder
import com.setu.saarthi.domain.usecase.ObserveSavedIdsUseCase
import com.setu.saarthi.domain.usecase.ToggleSavedSchemeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ResultsUiState(
    val matchResult: MatchResult? = null,
    val savedIds: Set<String> = emptySet()
)

class ResultsViewModel(
    private val querySessionHolder: QuerySessionHolder,
    private val observeSavedIds: ObserveSavedIdsUseCase,
    private val toggleSavedScheme: ToggleSavedSchemeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(querySessionHolder.session, observeSavedIds()) { session, savedIds ->
                ResultsUiState(matchResult = session.matchResult, savedIds = savedIds)
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onToggleSave(schemeId: String) {
        viewModelScope.launch { toggleSavedScheme(schemeId) }
    }
}
