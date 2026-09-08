package com.setu.saarthi.domain.model

enum class Gender { MALE, FEMALE, OTHER }
enum class SocialCategory { SC, ST, OBC, GEN, MINORITY, EWS, PWD }
enum class SchemeCategory { MSME, AGRICULTURE, EDUCATION, HOUSING, SOCIAL_WELFARE, WOMEN, MINORITY, RURAL, URBAN, FINANCIAL }
enum class FinancingType { LOAN, SUBSIDY, GRANT, COMPOSITE, GUARANTEE, INSURANCE, PENSION, SCHOLARSHIP, TRAINING }

enum class CustomOperator { GT, GTE, LT, LTE, EQ, NEQ, IN, NOT_IN, BETWEEN }

data class AgeRule(val min: Int? = null, val max: Int? = null)
data class IncomeRule(val min: Long? = null, val max: Long? = null)
data class LocationRule(val states: List<String>? = null, val urbanOnly: Boolean? = null, val ruralOnly: Boolean? = null)

sealed interface CustomRuleValue {
    data class Num(val value: Double) : CustomRuleValue
    data class Str(val value: String) : CustomRuleValue
    data class StrList(val value: List<String>) : CustomRuleValue
    data class Range(val min: Double, val max: Double) : CustomRuleValue
}
data class CustomRule(val field: String, val operator: CustomOperator, val value: CustomRuleValue, val message: String)

data class Eligibility(
    val age: AgeRule? = null,
    val incomeAnnual: IncomeRule? = null,
    val gender: List<Gender>? = null,
    val socialCategories: List<SocialCategory>? = null,
    val activities: List<String>? = null,
    val activityCategories: List<String>? = null,
    val location: LocationRule? = null,
    val occupation: List<String>? = null,
    val existingBusiness: Boolean? = null,
    val disability: Boolean? = null,
    val minority: Boolean? = null,
    val minProjectCost: Long? = null,
    val maxProjectCost: Long? = null,
    val customRules: List<CustomRule> = emptyList()
)

data class InterestRate(val base: Double, val subsidyRate: Double? = null, val effectiveRate: Double)
data class TenureRange(val min: Int, val max: Int)

data class Financing(
    val type: FinancingType,
    val maxAmount: Long? = null,
    val minAmount: Long? = null,
    val interestRate: InterestRate,
    val ownContributionPct: Int = 0,
    val tenureMonths: TenureRange,
    val moratoriumMonths: Int = 0,
    val collateralRequired: Boolean = false,
    val subsidyAmount: Long? = null,
    val subsidyPct: Int? = null,
    val subsidyNotes: String? = null
)

data class SchemeDocument(val id: String, val name: String, val required: Boolean, val note: String? = null)

data class ChannelPartners(
    val types: List<String> = emptyList(),
    val pmSurajIntegrated: Boolean = false,
    val specificBanks: List<String> = emptyList()
)

data class SchemeMetadata(
    val active: Boolean,
    val version: String,
    val officialUrl: String,
    val applicationPortal: String? = null,
    val nodalAgency: String? = null,
    val helpline: String? = null
)

data class Scheme(
    val schemeId: String,
    val name: String,
    val shortName: String,
    val ministry: String,
    val category: SchemeCategory,
    val description: String,
    val tags: List<String> = emptyList(),
    val eligibility: Eligibility,
    val financing: Financing,
    val documents: List<SchemeDocument>,
    val channelPartners: ChannelPartners,
    val metadata: SchemeMetadata
)
