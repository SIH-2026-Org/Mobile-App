package com.setu.saarthi.domain.engine

import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.MatchResult
import com.setu.saarthi.domain.model.ScoringWeights

/** Options mirroring `matchSchemes(profile, options)`'s optional overrides. */
data class MatchOptions(
    val weights: ScoringWeights = ScoringWeights(),
    val topN: Int = 5,
    val nearMissN: Int = 3,
    val runSimulation: Boolean = true,
    val runDocuments: Boolean = true
)

/**
 * The single orchestration entry point for the "Structured Requirements ->
 * Eligible Schemes" step, ported from `rule.engine.js#matchSchemes()`. Wires
 * together [EligibilityEngine], [ScoringEngine], [FinancialSimulator], and
 * [DocumentChecklistGenerator] over the [com.setu.saarthi.domain.repository.SchemeRepository]'s
 * scheme list. AI/LLM is not involved anywhere in this call - all decisions
 * are deterministic and reproducible.
 */
interface SchemeMatchingEngine {
    suspend fun match(profile: BeneficiaryProfile, options: MatchOptions = MatchOptions()): MatchResult
}
