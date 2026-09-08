package com.setu.saarthi.domain.model

enum class AreaType { URBAN, RURAL }

/**
 * The structured profile the eligibility/scoring/simulation engines operate
 * on, ported from the reference engine's `BeneficiaryProfile` typedef
 * (`rule.engine.js`). Built per-query by merging the persisted [UserProfile]
 * with whatever [com.setu.saarthi.domain.llm.RequirementExtractor] pulls out
 * of the current free-text message - fresh extraction wins over stored
 * profile values, mirroring `profile.builder.js`'s `buildProfile()`.
 */
data class BeneficiaryProfile(
    // Identity
    val age: Int? = null,
    val gender: Gender? = null,
    val socialCategory: SocialCategory? = null,
    val disability: Boolean? = null,

    // Location
    val state: String? = null,
    val district: String? = null,
    val areaType: AreaType? = null,

    // Economic
    val incomeAnnual: Long? = null,
    val existingLoans: Boolean? = null,

    // Business / activity
    val activity: String? = null,
    val activityCategory: String? = null,
    val existingBusiness: Boolean? = null,
    val businessAgeYears: Int? = null,
    val employeesCount: Int? = null,

    // Financial need
    val projectCost: Long? = null,
    val loanRequired: Long? = null,
    val purpose: String? = null
) {
    /** Reads a field generically by name, matching the JS engine's custom-rule field lookup. */
    fun fieldValue(field: String): Any? = when (field) {
        "age" -> age
        "gender" -> gender
        "socialCategory", "social_category" -> socialCategory
        "disability" -> disability
        "state" -> state
        "district" -> district
        "areaType", "area_type" -> areaType
        "incomeAnnual", "income_annual" -> incomeAnnual
        "existingLoans", "existing_loans" -> existingLoans
        "activity" -> activity
        "activityCategory", "activity_category" -> activityCategory
        "existingBusiness", "existing_business" -> existingBusiness
        "businessAgeYears", "business_age_years" -> businessAgeYears
        "employeesCount", "employees_count" -> employeesCount
        "projectCost", "project_cost" -> projectCost
        "loanRequired", "loan_required" -> loanRequired
        "purpose" -> purpose
        else -> null
    }
}

/**
 * Merges freshly-extracted fields over a base profile - non-null values in
 * [extracted] win, matching `buildProfile()`'s "fresh extraction overrides
 * session data" rule.
 */
fun BeneficiaryProfile.mergedWith(extracted: BeneficiaryProfile): BeneficiaryProfile = BeneficiaryProfile(
    age = extracted.age ?: age,
    gender = extracted.gender ?: gender,
    socialCategory = extracted.socialCategory ?: socialCategory,
    disability = extracted.disability ?: disability,
    state = extracted.state ?: state,
    district = extracted.district ?: district,
    areaType = extracted.areaType ?: areaType,
    incomeAnnual = extracted.incomeAnnual ?: incomeAnnual,
    existingLoans = extracted.existingLoans ?: existingLoans,
    activity = extracted.activity ?: activity,
    activityCategory = extracted.activityCategory ?: activityCategory,
    existingBusiness = extracted.existingBusiness ?: existingBusiness,
    businessAgeYears = extracted.businessAgeYears ?: businessAgeYears,
    employeesCount = extracted.employeesCount ?: employeesCount,
    projectCost = extracted.projectCost ?: projectCost,
    loanRequired = extracted.loanRequired ?: loanRequired,
    purpose = extracted.purpose ?: purpose
)
