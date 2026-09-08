package com.setu.saarthi.domain.model

/** Weighted score breakdown, ported from `scoring.engine.js`'s six-factor model. */
data class ScoreBreakdown(
    val eligibilityFit: Int,
    val financialFit: Int,
    val activityFit: Int,
    val preferenceFit: Int,
    val channelAvailability: Int,
    val locationAccessibility: Int
)

data class SchemeScore(
    val schemeId: String,
    val totalScore: Int,
    val breakdown: ScoreBreakdown,
    val reasons: List<String>,
    val warnings: List<String>
)

data class ScoringWeights(
    val eligibilityFit: Int = 30,
    val financialFit: Int = 25,
    val activityFit: Int = 15,
    val preferenceFit: Int = 10,
    val channelAvailability: Int = 10,
    val locationAccessibility: Int = 10
)
