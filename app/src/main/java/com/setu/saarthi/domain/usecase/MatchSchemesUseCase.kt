package com.setu.saarthi.domain.usecase

import com.setu.saarthi.domain.engine.MatchOptions
import com.setu.saarthi.domain.engine.SchemeMatchingEngine
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.MatchResult
import com.setu.saarthi.domain.model.asBeneficiaryProfile
import com.setu.saarthi.domain.model.mergedWith
import com.setu.saarthi.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first

/**
 * The "Structured Requirements -> Rule Engine -> Eligible Schemes" step.
 * Seeds the persisted onboarding [com.setu.saarthi.domain.model.UserProfile]
 * as a base, then lets the freshly-extracted [BeneficiaryProfile] override
 * any field it found - matching the reference engine's session+extraction
 * merge order.
 */
class MatchSchemesUseCase(
    private val schemeMatchingEngine: SchemeMatchingEngine,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(extracted: BeneficiaryProfile, options: MatchOptions = MatchOptions()): MatchResult {
        val storedProfile = profileRepository.observeProfile().first().asBeneficiaryProfile()
        val merged = storedProfile.mergedWith(extracted)
        return schemeMatchingEngine.match(merged, options)
    }
}
