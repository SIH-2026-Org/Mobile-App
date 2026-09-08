package com.setu.saarthi.domain.model

enum class RuleStatus { PASS, FAIL, MISSING }

enum class EligibilityStatus { ELIGIBLE, NOT_ELIGIBLE, NEEDS_MORE_INFO }

/** One rule's outcome, ported from the reference engine's per-rule evaluator results. */
data class RuleCheck(val rule: String, val status: RuleStatus, val message: String)

/**
 * Full eligibility breakdown for one scheme against one profile, ported from
 * `eligibility.engine.js`'s `evaluateScheme()` return shape.
 */
data class EligibilityResult(
    val schemeId: String,
    val status: EligibilityStatus,
    val passed: List<RuleCheck>,
    val failed: List<RuleCheck>,
    val missing: List<RuleCheck>,
    /** 0..100 - percentage of applicable (pass+fail) rules that passed. */
    val confidence: Int,
    val totalRulesChecked: Int
)

data class EvaluatedSchemes(
    val eligible: List<Pair<Scheme, EligibilityResult>>,
    val partial: List<Pair<Scheme, EligibilityResult>>,
    val ineligible: List<Pair<Scheme, EligibilityResult>>
)
