package com.setu.saarthi.domain.engine

import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.EligibilityResult
import com.setu.saarthi.domain.model.EvaluatedSchemes
import com.setu.saarthi.domain.model.Scheme

/**
 * THE RULE ENGINE. Pure, deterministic evaluator - no I/O, no side effects,
 * no AI involved in eligibility decisions. Ported from the reference
 * engine's `eligibility.engine.js`.
 */
interface EligibilityEngine {
    fun evaluateScheme(profile: BeneficiaryProfile, scheme: Scheme): EligibilityResult
    fun evaluateAllSchemes(profile: BeneficiaryProfile, schemes: List<Scheme>): EvaluatedSchemes
}
