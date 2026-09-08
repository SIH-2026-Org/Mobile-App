package com.setu.saarthi.domain.session

import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.MatchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory bridge carrying the current query's [BeneficiaryProfile] and
 * [MatchResult] from Home -> Clarify -> Results -> Detail, so those screens
 * don't have to serialize the full match payload through nav arguments.
 * A single Koin instance for the process lifetime - intentionally not
 * persisted, since a query is only ever meaningful for the current session.
 */
class QuerySessionHolder {
    private val _session = MutableStateFlow(QuerySession())
    val session: StateFlow<QuerySession> = _session.asStateFlow()

    fun setRawText(text: String) {
        _session.update { QuerySession(rawText = text) }
    }

    fun setProfile(profile: BeneficiaryProfile) {
        _session.update { it.copy(profile = profile) }
    }

    fun setMatchResult(result: MatchResult) {
        _session.update { it.copy(matchResult = result) }
    }

    fun clear() {
        _session.update { QuerySession() }
    }
}

data class QuerySession(
    val rawText: String = "",
    val profile: BeneficiaryProfile? = null,
    val matchResult: MatchResult? = null
)
