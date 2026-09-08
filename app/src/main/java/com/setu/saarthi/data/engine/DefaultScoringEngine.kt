package com.setu.saarthi.data.engine

import com.setu.saarthi.domain.engine.ScoringEngine
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.EligibilityResult
import com.setu.saarthi.domain.model.Gender
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.model.SchemeScore
import com.setu.saarthi.domain.model.ScoreBreakdown
import com.setu.saarthi.domain.model.ScoringWeights
import kotlin.math.roundToInt

/** Ported from the reference engine's `scoring.engine.js`. */
class DefaultScoringEngine : ScoringEngine {

    private data class FactorResult(val score: Int, val reasons: List<String>, val warnings: List<String>)

    private fun clamp(value: Int, min: Int = 0, max: Int = 100): Int = value.coerceIn(min, max)

    private fun rupees(amount: Long): String = "₹" + "%,d".format(amount)

    private fun scoreEligibilityFit(result: EligibilityResult, weight: Int): FactorResult {
        val score = clamp(Math.round((result.confidence / 100.0) * weight).toInt())
        val reasons = result.passed.map { "✓ ${it.message}" }
        val warnings = result.failed.map { "✗ ${it.message}" } + result.missing.map { "⚠ ${it.message}" }
        return FactorResult(score, reasons, warnings)
    }

    private fun scoreFinancialFit(profile: BeneficiaryProfile, scheme: Scheme, weight: Int): FactorResult {
        val financing = scheme.financing
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val need = profile.loanRequired ?: profile.projectCost
        if (need == null) {
            warnings += "⚠ Project cost not provided - financial fit estimated at 50%."
            return FactorResult((weight * 0.5).roundToInt(), reasons, warnings)
        }

        var score: Int
        val max = financing.maxAmount
        if (max == null) {
            score = weight
            reasons += "✓ No upper financing limit - can cover ${rupees(need)}."
        } else if (need <= max) {
            val coverageRatio = need.toDouble() / max
            val fitScore = if (coverageRatio >= 0.3) 1.0 else coverageRatio / 0.3
            score = clamp((fitScore * weight).roundToInt())
            reasons += "✓ Financing up to ${rupees(max)} covers your requirement of ${rupees(need)}."
        } else {
            val coverage = max.toDouble() / need
            score = clamp((coverage * weight * 0.7).roundToInt())
            warnings += "⚠ Scheme maximum ${rupees(max)} covers only ${(coverage * 100).roundToInt()}% of your requirement of ${rupees(need)}."
        }

        val rate = financing.interestRate.effectiveRate.takeIf { it > 0 } ?: financing.interestRate.base
        when {
            rate <= 4 -> {
                score = clamp(score + (weight * 0.1).roundToInt())
                reasons += "✓ Very low effective interest rate: $rate% p.a."
            }
            rate <= 7 -> reasons += "✓ Competitive interest rate: $rate% p.a."
            rate <= 12 -> warnings += "⚠ Moderate interest rate: $rate% p.a."
            else -> {
                score = clamp(score - (weight * 0.1).roundToInt())
                warnings += "⚠ Higher interest rate: $rate% p.a."
            }
        }

        if (financing.subsidyAmount != null || financing.subsidyPct != null) {
            val subsDesc = financing.subsidyAmount?.let { "${rupees(it)} capital subsidy" }
                ?: "${financing.subsidyPct}% capital subsidy"
            score = clamp(score + (weight * 0.1).roundToInt())
            reasons += "✓ Includes $subsDesc."
        }

        if (!financing.collateralRequired) {
            reasons += "✓ No collateral required."
        } else {
            warnings += "⚠ Collateral / security required."
        }

        return FactorResult(clamp(score), reasons, warnings)
    }

    private fun scoreActivityFit(profile: BeneficiaryProfile, scheme: Scheme, weight: Int): FactorResult {
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (profile.activity == null && profile.activityCategory == null) {
            return FactorResult((weight * 0.5).roundToInt(), reasons, warnings)
        }

        val schemeActivities = scheme.eligibility.activities
        if (schemeActivities.isNullOrEmpty()) {
            reasons += "✓ Scheme accepts all business activities."
            return FactorResult((weight * 0.7).roundToInt(), reasons, warnings)
        }

        val activity = profile.activity
        val matched = activity != null && schemeActivities.any { a ->
            val p = activity.lowercase(); val s = a.lowercase()
            p == s || p.contains(s) || s.contains(p)
        }

        val score: Int
        if (matched) {
            score = weight
            reasons += "✓ Scheme is specifically designed for \"$activity\" activity."
        } else {
            val tagMatch = activity != null && scheme.tags.any { activity.lowercase().contains(it.lowercase()) }
            if (tagMatch) {
                score = (weight * 0.6).roundToInt()
                reasons += "✓ Activity partially matches scheme scope."
            } else {
                score = (weight * 0.3).roundToInt()
                warnings += "⚠ Activity \"$activity\" may not be the primary focus of this scheme."
            }
        }

        return FactorResult(clamp(score), reasons, warnings)
    }

    private fun scorePreferenceFit(profile: BeneficiaryProfile, scheme: Scheme, weight: Int): FactorResult {
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        var score = (weight * 0.5).roundToInt()

        val schemeCategories = scheme.eligibility.socialCategories
        if (schemeCategories != null && schemeCategories.size < 4 && profile.socialCategory != null) {
            if (schemeCategories.contains(profile.socialCategory)) {
                score = clamp(score + (weight * 0.3).roundToInt())
                reasons += "✓ Scheme specifically targets ${profile.socialCategory} category - strong preference match."
            }
        }

        val genderRule = scheme.eligibility.gender
        if (genderRule != null && genderRule.size == 1 && genderRule[0] == Gender.FEMALE) {
            if (profile.gender == Gender.FEMALE) {
                score = clamp(score + (weight * 0.2).roundToInt())
                reasons += "✓ Women-specific scheme - aligned with your profile."
            }
        }

        val purpose = profile.purpose
        if (purpose != null && scheme.tags.isNotEmpty()) {
            val purposeNorm = purpose.lowercase().replace("_", " ")
            val tagMatch = scheme.tags.any { purposeNorm.contains(it.lowercase()) || it.lowercase().contains(purposeNorm) }
            if (tagMatch) {
                score = clamp(score + (weight * 0.2).roundToInt())
                reasons += "✓ Scheme purpose aligns with your goal."
            }
        }

        return FactorResult(clamp(score), reasons, warnings)
    }

    private fun scoreChannelAvailability(scheme: Scheme, weight: Int): FactorResult {
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val partnerCount = scheme.channelPartners.types.size
        var score = clamp((((minOf(partnerCount, 5).toDouble()) / 5) * weight).roundToInt())

        if (scheme.channelPartners.pmSurajIntegrated) {
            score = clamp(score + (weight * 0.3).roundToInt())
            reasons += "✓ Available on PM-SURAJ portal - easy online application."
        }

        if (partnerCount >= 3) {
            reasons += "✓ Wide partner network: ${scheme.channelPartners.types.joinToString(", ")}."
        } else if (partnerCount == 1) {
            warnings += "⚠ Limited to a single partner type: ${scheme.channelPartners.types.first()}."
        }

        if (scheme.channelPartners.specificBanks.isNotEmpty()) {
            reasons += "✓ Specific banks: ${scheme.channelPartners.specificBanks.take(3).joinToString(", ")}."
        }

        return FactorResult(clamp(score), reasons, warnings)
    }

    private fun scoreLocationAccessibility(profile: BeneficiaryProfile, scheme: Scheme, weight: Int): FactorResult {
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val locationRule = scheme.eligibility.location

        if (locationRule?.states.isNullOrEmpty() || locationRule?.states?.any { it.equals("all", ignoreCase = true) } == true) {
            reasons += "✓ Available PAN India - no geographic restriction."
            return FactorResult(weight, reasons, warnings)
        }

        val states = locationRule.states!!
        val state = profile.state
        if (state == null) {
            warnings += "⚠ Scheme available in specific states: ${states.take(5).joinToString(", ")}${if (states.size > 5) "..." else ""}."
            return FactorResult((weight * 0.5).roundToInt(), reasons, warnings)
        }

        val stateNorm = state.lowercase()
        val inState = states.any { it.lowercase().contains(stateNorm) || stateNorm.contains(it.lowercase()) }
        if (inState) {
            reasons += "✓ Available in $state."
            return FactorResult(weight, reasons, warnings)
        }

        warnings += "⚠ Scheme not available in $state. Eligible states: ${states.take(4).joinToString(", ")}."
        return FactorResult(0, reasons, warnings)
    }

    override fun scoreScheme(
        profile: BeneficiaryProfile,
        scheme: Scheme,
        eligibilityResult: EligibilityResult,
        weights: ScoringWeights
    ): SchemeScore {
        val eligFit = scoreEligibilityFit(eligibilityResult, weights.eligibilityFit)
        val finFit = scoreFinancialFit(profile, scheme, weights.financialFit)
        val actFit = scoreActivityFit(profile, scheme, weights.activityFit)
        val prefFit = scorePreferenceFit(profile, scheme, weights.preferenceFit)
        val chanAvail = scoreChannelAvailability(scheme, weights.channelAvailability)
        val locAccess = scoreLocationAccessibility(profile, scheme, weights.locationAccessibility)

        val total = clamp(eligFit.score + finFit.score + actFit.score + prefFit.score + chanAvail.score + locAccess.score)

        return SchemeScore(
            schemeId = scheme.schemeId,
            totalScore = total,
            breakdown = ScoreBreakdown(
                eligibilityFit = eligFit.score,
                financialFit = finFit.score,
                activityFit = actFit.score,
                preferenceFit = prefFit.score,
                channelAvailability = chanAvail.score,
                locationAccessibility = locAccess.score
            ),
            reasons = (eligFit.reasons + finFit.reasons + actFit.reasons + prefFit.reasons + chanAvail.reasons + locAccess.reasons).filter { it.isNotBlank() },
            warnings = (eligFit.warnings + finFit.warnings + actFit.warnings + prefFit.warnings + chanAvail.warnings + locAccess.warnings).filter { it.isNotBlank() }
        )
    }
}
