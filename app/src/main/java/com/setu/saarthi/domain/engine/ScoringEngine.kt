package com.setu.saarthi.domain.engine

import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.EligibilityResult
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.model.SchemeScore
import com.setu.saarthi.domain.model.ScoringWeights

/**
 * Weighted multi-factor scoring/ranking, ported from `scoring.engine.js`.
 * Produces a 0-100 total score with a per-dimension breakdown, reasons and
 * warnings.
 */
interface ScoringEngine {
    fun scoreScheme(
        profile: BeneficiaryProfile,
        scheme: Scheme,
        eligibilityResult: EligibilityResult,
        weights: ScoringWeights = ScoringWeights()
    ): SchemeScore

    fun rankScores(scores: List<SchemeScore>): List<SchemeScore> = scores.sortedByDescending { it.totalScore }
}
