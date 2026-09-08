package com.setu.saarthi.data.engine

import com.setu.saarthi.domain.engine.DocumentChecklistGenerator
import com.setu.saarthi.domain.engine.EligibilityEngine
import com.setu.saarthi.domain.engine.FinancialSimulator
import com.setu.saarthi.domain.engine.MatchOptions
import com.setu.saarthi.domain.engine.ScoringEngine
import com.setu.saarthi.domain.engine.SchemeMatchingEngine
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.EligibilityResult
import com.setu.saarthi.domain.model.MatchResult
import com.setu.saarthi.domain.model.MatchStatus
import com.setu.saarthi.domain.model.MatchedScheme
import com.setu.saarthi.domain.model.NearMissScheme
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.repository.SchemeRepository

/**
 * Wires [EligibilityEngine] + [ScoringEngine] + [FinancialSimulator] +
 * [DocumentChecklistGenerator] into the single "Structured Requirements ->
 * Eligible Schemes" call, ported from the reference engine's
 * `rule.engine.js#matchSchemes()`. AI/LLM is not involved in eligibility
 * decisions - every decision here is deterministic and reproducible.
 */
class DefaultSchemeMatchingEngine(
    private val schemeRepository: SchemeRepository,
    private val eligibilityEngine: EligibilityEngine,
    private val scoringEngine: ScoringEngine,
    private val financialSimulator: FinancialSimulator,
    private val documentChecklistGenerator: DocumentChecklistGenerator
) : SchemeMatchingEngine {

    private companion object {
        val CLARIFICATION_QUESTIONS = mapOf(
            "age" to "How old are you? (Please share your age in years)",
            "gender" to "What is your gender? (Male / Female / Other)",
            "social_category" to "What is your social category? (General / SC / ST / OBC / Minority / EWS)",
            "state" to "Which state do you live in?",
            "area_type" to "Do you live in an urban area (town/city) or a rural area (village)?",
            "income_annual" to "What is your approximate annual family income? (in rupees)",
            "activity" to "What type of business or work are you planning to do?",
            "project_cost" to "What is the estimated total cost of your project? (in rupees)",
            "existing_business" to "Do you already have an existing business, or are you starting a new one?",
            "loan_required" to "How much loan amount do you need? (in rupees)"
        )
        val CRITICAL_FIELDS = listOf("activity", "project_cost", "state")
    }

    private fun detectMissingFields(profile: BeneficiaryProfile, missingFromEngine: List<String>): Pair<List<String>, List<String>> {
        val fields = mutableListOf<String>()
        val questions = mutableListOf<String>()

        for (field in CRITICAL_FIELDS) {
            if (profile.fieldValue(field) == null && field !in fields) {
                fields += field
                CLARIFICATION_QUESTIONS[field]?.let { questions += it }
            }
        }

        for (rule in missingFromEngine) {
            val fieldName = rule.removePrefix("custom_")
            if (fieldName !in fields && CLARIFICATION_QUESTIONS.containsKey(fieldName)) {
                fields += fieldName
                CLARIFICATION_QUESTIONS[fieldName]?.let { questions += it }
            }
        }

        return fields to questions
    }

    private fun rankAndAttach(
        profile: BeneficiaryProfile,
        results: List<Pair<Scheme, EligibilityResult>>,
        weights: com.setu.saarthi.domain.model.ScoringWeights,
        limit: Int,
        runSim: Boolean,
        runDocs: Boolean
    ): List<MatchedScheme> {
        val scored = results.map { (scheme, elig) -> Triple(scheme, elig, scoringEngine.scoreScheme(profile, scheme, elig, weights)) }
        val ranked = scored.sortedByDescending { it.third.totalScore }.take(limit)
        return ranked.mapIndexed { index, (scheme, elig, score) ->
            MatchedScheme(
                rank = index + 1,
                scheme = scheme,
                eligibility = elig,
                score = score,
                simulation = if (runSim) financialSimulator.simulate(profile, scheme) else null,
                documents = if (runDocs) documentChecklistGenerator.generateChecklist(profile, scheme) else null
            )
        }
    }

    override suspend fun match(profile: BeneficiaryProfile, options: MatchOptions): MatchResult {
        val schemesResult = schemeRepository.getAll()
        val schemes = schemesResult.getOrElse {
            return MatchResult(
                status = MatchStatus.ERROR,
                missingFields = emptyList(),
                clarificationQuestions = emptyList(),
                eligibleSchemes = emptyList(),
                partialSchemes = emptyList(),
                nearMissSchemes = emptyList(),
                profile = profile,
                totalSchemesChecked = 0
            )
        }

        val evaluated = eligibilityEngine.evaluateAllSchemes(profile, schemes)

        val allMissingRules = evaluated.partial.flatMap { (_, result) -> result.missing.map { it.rule } }
        val (missingFields, clarificationQuestions) = detectMissingFields(profile, allMissingRules)

        val topEligible = rankAndAttach(profile, evaluated.eligible, options.weights, options.topN, options.runSimulation, options.runDocuments)
        val remaining = maxOf(0, options.topN - topEligible.size)
        val topPartial = rankAndAttach(profile, evaluated.partial, options.weights, remaining, options.runSimulation, options.runDocuments)

        val nearMisses = evaluated.ineligible
            .sortedByDescending { (_, result) -> result.passed.size }
            .take(options.nearMissN)
            .map { (scheme, result) -> NearMissScheme(scheme, result, result.failed.map { it.message }) }

        val status = when {
            topEligible.isNotEmpty() -> MatchStatus.OK
            topPartial.isNotEmpty() || missingFields.isNotEmpty() -> MatchStatus.NEEDS_MORE_INFO
            else -> MatchStatus.NO_MATCH
        }

        return MatchResult(
            status = status,
            missingFields = missingFields,
            clarificationQuestions = clarificationQuestions,
            eligibleSchemes = topEligible,
            partialSchemes = topPartial,
            nearMissSchemes = nearMisses,
            profile = profile,
            totalSchemesChecked = schemes.count { it.metadata.active }
        )
    }
}
