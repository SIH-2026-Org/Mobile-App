package com.setu.saarthi.domain.model

/**
 * The onboarding profile, persisted via [com.setu.saarthi.domain.repository.ProfileRepository]
 * (DataStore-backed). Converted to a [BeneficiaryProfile] via [asBeneficiaryProfile]
 * to seed each query, then overridden field-by-field by whatever the current
 * message's extraction finds (see [mergedWith]).
 */
data class UserProfile(
    val name: String = "",
    val age: Int? = null,
    val gender: Gender? = null,
    val state: String? = null,
    val district: String? = null,
    val areaType: AreaType? = null,
    val activity: String? = null,
    val activityCategory: String? = null,
    val annualIncome: Long? = null,
    val socialCategory: SocialCategory? = null,
    val disability: Boolean? = null,
    val hasExistingBusiness: Boolean? = null,
    val onboardingComplete: Boolean = false
)

fun UserProfile.asBeneficiaryProfile(): BeneficiaryProfile = BeneficiaryProfile(
    age = age,
    gender = gender,
    socialCategory = socialCategory,
    disability = disability,
    state = state,
    district = district,
    areaType = areaType,
    incomeAnnual = annualIncome,
    activity = activity,
    activityCategory = activityCategory,
    existingBusiness = hasExistingBusiness
)
