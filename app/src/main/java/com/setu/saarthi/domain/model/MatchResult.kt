package com.setu.saarthi.domain.model

enum class MatchStatus { OK, NEEDS_MORE_INFO, NO_MATCH, ERROR }

/** One ranked, enriched scheme in a [MatchResult], ported from `rule.engine.js`'s `MatchedScheme`. */
data class MatchedScheme(
    val rank: Int,
    val scheme: Scheme,
    val eligibility: EligibilityResult,
    val score: SchemeScore,
    val simulation: FinancialSimulation?,
    val documents: DocumentChecklist?
)

data class NearMissScheme(
    val scheme: Scheme,
    val eligibility: EligibilityResult,
    val failureReasons: List<String>
)

/**
 * Top-level result of [com.setu.saarthi.domain.engine.SchemeMatchingEngine.match],
 * ported from `rule.engine.js`'s `matchSchemes()` return shape - the "Structured
 * Requirements -> Eligible Schemes" step of the core flow.
 */
data class MatchResult(
    val status: MatchStatus,
    val missingFields: List<String>,
    val clarificationQuestions: List<String>,
    val eligibleSchemes: List<MatchedScheme>,
    val partialSchemes: List<MatchedScheme>,
    val nearMissSchemes: List<NearMissScheme>,
    val profile: BeneficiaryProfile,
    val totalSchemesChecked: Int
)
