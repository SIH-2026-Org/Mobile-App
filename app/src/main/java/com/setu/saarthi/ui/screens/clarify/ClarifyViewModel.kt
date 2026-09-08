package com.setu.saarthi.ui.screens.clarify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.MatchStatus
import com.setu.saarthi.domain.model.mergedWith
import com.setu.saarthi.domain.session.QuerySessionHolder
import com.setu.saarthi.domain.usecase.ExtractRequirementUseCase
import com.setu.saarthi.domain.usecase.MatchSchemesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(val id: String, val fromUser: Boolean, val text: String)

data class ClarifyUiState(
    val messages: List<ChatMessage> = emptyList(),
    val answerText: String = "",
    val isThinking: Boolean = true,
    val isDone: Boolean = false,
    val errorMessage: String? = null
)

/**
 * The chat-style clarification flow: walks the [com.setu.saarthi.domain.model.MatchResult.clarificationQuestions]
 * one at a time, feeding each free-text answer back through the LLM layer
 * and re-running the rule engine until it no longer needs more info.
 */
class ClarifyViewModel(
    private val querySessionHolder: QuerySessionHolder,
    private val extractRequirement: ExtractRequirementUseCase,
    private val matchSchemes: MatchSchemesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClarifyUiState())
    val uiState: StateFlow<ClarifyUiState> = _uiState.asStateFlow()

    private var draftProfile: BeneficiaryProfile = querySessionHolder.session.value.matchResult?.profile ?: BeneficiaryProfile()

    init {
        val firstQuestion = querySessionHolder.session.value.matchResult?.clarificationQuestions?.firstOrNull()
        if (firstQuestion == null) {
            _uiState.update { it.copy(isThinking = false, isDone = true) }
        } else {
            _uiState.update { it.copy(isThinking = false, messages = listOf(assistantMessage(firstQuestion))) }
        }
    }

    private fun assistantMessage(text: String) = ChatMessage(UUID.randomUUID().toString(), fromUser = false, text = text)
    private fun userMessage(text: String) = ChatMessage(UUID.randomUUID().toString(), fromUser = true, text = text)

    fun onAnswerChange(text: String) {
        _uiState.update { it.copy(answerText = text) }
    }

    fun onSubmitAnswer() {
        val answer = _uiState.value.answerText
        if (answer.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    messages = it.messages + userMessage(answer),
                    answerText = "",
                    isThinking = true,
                    errorMessage = null
                )
            }

            val extraction = extractRequirement(answer)
            extraction.fold(
                onSuccess = { extracted ->
                    draftProfile = draftProfile.mergedWith(extracted)
                    val result = matchSchemes(draftProfile)
                    querySessionHolder.setMatchResult(result)

                    val nextQuestion = result.clarificationQuestions.firstOrNull()
                    if (result.status == MatchStatus.NEEDS_MORE_INFO && nextQuestion != null) {
                        _uiState.update { it.copy(isThinking = false, messages = it.messages + assistantMessage(nextQuestion)) }
                    } else {
                        _uiState.update { it.copy(isThinking = false, isDone = true) }
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isThinking = false, errorMessage = e.message ?: "Could not understand that.") }
                }
            )
        }
    }
}
