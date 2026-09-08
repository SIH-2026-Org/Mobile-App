package com.setu.saarthi.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.MatchStatus
import com.setu.saarthi.domain.model.NewsItem
import com.setu.saarthi.domain.session.QuerySessionHolder
import com.setu.saarthi.domain.usecase.ExtractRequirementUseCase
import com.setu.saarthi.domain.usecase.GetNewsFeedUseCase
import com.setu.saarthi.domain.usecase.MatchSchemesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeNavTarget { RESULTS, CLARIFY }

data class HomeUiState(
    val isNewsLoading: Boolean = true,
    val newsError: String? = null,
    val news: List<NewsItem> = emptyList(),
    val inputText: String = "",
    val isParsing: Boolean = false,
    val isMatching: Boolean = false,
    val parseError: String? = null,
    val extracted: BeneficiaryProfile? = null,
    val navigateTo: HomeNavTarget? = null
)

class HomeViewModel(
    private val getNewsFeed: GetNewsFeedUseCase,
    private val extractRequirement: ExtractRequirementUseCase,
    private val matchSchemes: MatchSchemesUseCase,
    private val querySessionHolder: QuerySessionHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadNews()
    }

    private fun loadNews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isNewsLoading = true, newsError = null) }
            getNewsFeed().fold(
                onSuccess = { news -> _uiState.update { it.copy(isNewsLoading = false, news = news) } },
                onFailure = { e -> _uiState.update { it.copy(isNewsLoading = false, newsError = e.message ?: "Could not load news") } }
            )
        }
    }

    fun onRetryNews() = loadNews()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSend() {
        val text = _uiState.value.inputText
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isParsing = true, parseError = null, extracted = null) }
            querySessionHolder.setRawText(text)
            extractRequirement(text).fold(
                onSuccess = { profile -> _uiState.update { it.copy(isParsing = false, extracted = profile) } },
                onFailure = { e -> _uiState.update { it.copy(isParsing = false, parseError = e.message ?: "Could not understand that. Please try again.") } }
            )
        }
    }

    fun onExtractedFieldChange(profile: BeneficiaryProfile) {
        _uiState.update { it.copy(extracted = profile) }
    }

    fun onDismissRequirementSheet() {
        _uiState.update { it.copy(extracted = null) }
    }

    fun onConfirmRequirement() {
        val profile = _uiState.value.extracted ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isMatching = true) }
            val result = matchSchemes(profile)
            querySessionHolder.setMatchResult(result)
            val target = if (result.status == MatchStatus.NEEDS_MORE_INFO && result.clarificationQuestions.isNotEmpty()) {
                HomeNavTarget.CLARIFY
            } else {
                HomeNavTarget.RESULTS
            }
            _uiState.update { it.copy(isMatching = false, navigateTo = target) }
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(navigateTo = null, inputText = "", extracted = null) }
    }
}
