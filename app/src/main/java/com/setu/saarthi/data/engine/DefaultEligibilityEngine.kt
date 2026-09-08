package com.setu.saarthi.data.engine

import com.setu.saarthi.domain.engine.EligibilityEngine
import com.setu.saarthi.domain.model.AreaType
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.CustomOperator
import com.setu.saarthi.domain.model.CustomRule
import com.setu.saarthi.domain.model.CustomRuleValue
import com.setu.saarthi.domain.model.Eligibility
import com.setu.saarthi.domain.model.EligibilityResult
import com.setu.saarthi.domain.model.EligibilityStatus
import com.setu.saarthi.domain.model.EvaluatedSchemes
import com.setu.saarthi.domain.model.LocationRule
import com.setu.saarthi.domain.model.RuleCheck
import com.setu.saarthi.domain.model.RuleStatus
import com.setu.saarthi.domain.model.Scheme
import java.util.Locale

/**
 * Pure, deterministic evaluator ported from the reference engine's
 * `eligibility.engine.js`. No I/O, no side effects, no AI involved.
 */
class DefaultEligibilityEngine : EligibilityEngine {

    private companion object {
        val ACTIVITY_SYNONYMS: Map<String, List<String>> = mapOf(
            "dairy" to listOf("dairy", "milk", "cattle", "cow", "buffalo", "gau", "dugdh"),
            "poultry" to listOf("poultry", "chicken", "broiler", "layer", "egg"),
            "agriculture" to listOf("agriculture", "farming", "crop", "khet", "kisaan", "kisan"),
            "fishery" to listOf("fish", "fishery", "fisheries", "aquaculture", "machli"),
            "retail" to listOf("retail", "shop", "dukan", "store", "trader", "trading"),
            "street_vending" to listOf("street", "vending", "vendor", "rehdi", "thela", "hawker"),
            "tailoring" to listOf("tailoring", "tailor", "sewing", "stitching", "garment"),
            "weaving" to listOf("weaving", "weaver", "handloom", "loom", "bunkar"),
            "food" to listOf("food", "food processing", "catering", "restaurant", "dhaba", "bakery"),
            "beauty" to listOf("beauty", "salon", "parlour", "parlor", "barbershop"),
            "transport" to listOf("transport", "taxi", "auto", "rickshaw", "logistics"),
            "construction" to listOf("construction", "carpenter", "plumber", "mason", "electrician"),
            "handicraft" to listOf("handicraft", "craft", "artisan", "pottery", "sculpture"),
            "electronics" to listOf("electronics", "repair", "mobile repair", "computer"),
            "manufacturing" to listOf("manufacturing", "production", "factory", "unit"),
            "education" to listOf("education", "tuition", "coaching", "school", "college"),
            "health" to listOf("health", "clinic", "pharmacy", "medical")
        )
    }

    private fun norm(value: Any?): String = value?.toString()?.lowercase(Locale.ROOT)?.trim().orEmpty()

    private fun rupees(amount: Long): String = "₹" + "%,d".format(amount)

    private fun activitiesMatch(profileActivity: String?, schemeActivity: String): Boolean {
        val p = norm(profileActivity)
        val s = norm(schemeActivity)
        if (p.isEmpty() || s.isEmpty()) return false
        if (p == s) return true
        if (p.contains(s) || s.contains(p)) return true
        for ((canonical, aliases) in ACTIVITY_SYNONYMS) {
            val pMatches = aliases.any { p.contains(it) }
            val sMatches = aliases.any { s.contains(it) } || s == canonical
            if (pMatches && sMatches) return true
        }
        return false
    }

    private data class LocationCheck(val pass: Boolean, val missing: Boolean)

    private fun checkLocation(userState: String?, rule: LocationRule?): LocationCheck {
        if (rule == null) return LocationCheck(pass = true, missing = false)
        val states = rule.states
        if (states.isNullOrEmpty() || states.any { it.equals("all", ignoreCase = true) }) {
            return LocationCheck(pass = true, missing = false)
        }
        if (userState.isNullOrBlank()) return LocationCheck(pass = true, missing = true)
        val normState = norm(userState)
        val pass = states.any { norm(it) == normState || norm(it).contains(normState) || normState.contains(norm(it)) }
        return LocationCheck(pass = pass, missing = false)
    }

    private fun evalAge(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        val rule = eligibility.age ?: return null
        val age = profile.age
            ?: return RuleCheck("age", RuleStatus.MISSING, "Age not provided - required to verify eligibility.")
        val passMin = rule.min == null || age >= rule.min
        val passMax = rule.max == null || age <= rule.max
        return if (passMin && passMax) {
            RuleCheck("age", RuleStatus.PASS, "Age $age is within eligible range (${rule.min ?: ""}-${rule.max ?: ""} years).")
        } else {
            RuleCheck("age", RuleStatus.FAIL, "Age $age does not meet requirement: must be between ${rule.min ?: "any"} and ${rule.max ?: "any"} years.")
        }
    }

    private fun evalIncome(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        val rule = eligibility.incomeAnnual ?: return null
        val income = profile.incomeAnnual
            ?: return RuleCheck("income_annual", RuleStatus.MISSING, "Annual family income not provided - required to verify eligibility.")
        val passMax = rule.max == null || income <= rule.max
        val passMin = rule.min == null || income >= rule.min
        return if (passMax && passMin) {
            val cap = rule.max?.let { " (max ${rupees(it)})" }.orEmpty()
            RuleCheck("income_annual", RuleStatus.PASS, "Annual income ${rupees(income)} meets requirement$cap.")
        } else {
            RuleCheck("income_annual", RuleStatus.FAIL, "Annual income ${rupees(income)} exceeds the scheme limit of ${rule.max?.let { rupees(it) } ?: "N/A"}.")
        }
    }

    private fun evalGender(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        val rule = eligibility.gender
        if (rule.isNullOrEmpty()) return null
        val gender = profile.gender
            ?: return RuleCheck("gender", RuleStatus.MISSING, "Gender not provided - required for this scheme.")
        return if (rule.contains(gender)) {
            RuleCheck("gender", RuleStatus.PASS, "Gender \"$gender\" is eligible for this scheme.")
        } else {
            RuleCheck("gender", RuleStatus.FAIL, "This scheme is only for: ${rule.joinToString(", ")}. Your gender ($gender) is not eligible.")
        }
    }

    private fun evalSocialCategory(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        val rule = eligibility.socialCategories
        if (rule.isNullOrEmpty()) return null
        val category = profile.socialCategory
            ?: return RuleCheck("social_category", RuleStatus.MISSING, "Social category (SC/ST/OBC/GEN) not provided - required for this scheme.")
        return if (rule.contains(category)) {
            RuleCheck("social_category", RuleStatus.PASS, "Category \"$category\" is eligible for this scheme.")
        } else {
            RuleCheck("social_category", RuleStatus.FAIL, "This scheme is reserved for: ${rule.joinToString(", ")}. Your category ($category) is not eligible.")
        }
    }

    private fun evalActivity(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        val rule = eligibility.activities
        if (rule.isNullOrEmpty()) return null
        val activity = profile.activity
            ?: return RuleCheck("activity", RuleStatus.MISSING, "Business activity not provided - required to match scheme.")
        val matched = rule.any { activitiesMatch(activity, it) }
        return if (matched) {
            RuleCheck("activity", RuleStatus.PASS, "Business activity \"$activity\" is supported by this scheme.")
        } else {
            RuleCheck("activity", RuleStatus.FAIL, "Activity \"$activity\" is not covered. Scheme supports: ${rule.joinToString(", ")}.")
        }
    }

    private fun evalActivityCategory(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        val rule = eligibility.activityCategories
        if (rule.isNullOrEmpty()) return null
        val category = profile.activityCategory ?: return null
        val matched = rule.any { norm(it) == norm(category) }
        return if (matched) {
            RuleCheck("activity_category", RuleStatus.PASS, "Activity category \"$category\" is supported.")
        } else {
            RuleCheck("activity_category", RuleStatus.FAIL, "Activity category \"$category\" not supported. Eligible categories: ${rule.joinToString(", ")}.")
        }
    }

    private fun evalLocation(profile: BeneficiaryProfile, eligibility: Eligibility): List<RuleCheck>? {
        val rule = eligibility.location ?: return null
        val results = mutableListOf<RuleCheck>()

        val (statePass, stateMissing) = checkLocation(profile.state, rule)
        when {
            stateMissing -> results += RuleCheck("location_state", RuleStatus.MISSING, "State not provided - needed to confirm geographic eligibility.")
            !statePass -> results += RuleCheck(
                "location_state", RuleStatus.FAIL,
                "State \"${profile.state}\" is not in the eligible states for this scheme: ${rule.states?.joinToString(", ").orEmpty()}."
            )
            profile.state != null -> results += RuleCheck("location_state", RuleStatus.PASS, "State \"${profile.state}\" is eligible for this scheme.")
        }

        val areaType = profile.areaType
        when {
            rule.urbanOnly == true && areaType != null && areaType != AreaType.URBAN ->
                results += RuleCheck("location_area", RuleStatus.FAIL, "This scheme is only for urban areas.")
            rule.ruralOnly == true && areaType != null && areaType != AreaType.RURAL ->
                results += RuleCheck("location_area", RuleStatus.FAIL, "This scheme is only for rural areas.")
            rule.urbanOnly == true || rule.ruralOnly == true -> {
                val required = if (rule.urbanOnly == true) "urban" else "rural"
                if (areaType == null) {
                    results += RuleCheck("location_area", RuleStatus.MISSING, "Area type (urban/rural) required - scheme is $required-only.")
                } else {
                    results += RuleCheck("location_area", RuleStatus.PASS, "Area type \"$areaType\" is eligible.")
                }
            }
        }

        return results.ifEmpty { null }
    }

    /**
     * The reference engine's `BeneficiaryProfile` never actually populates an
     * `occupation` field (neither the typedef nor `profile.builder.js` define
     * one), so `evalOccupation` there is effectively dead - it always sees
     * `undefined` and soft-passes. Kept as a no-op here for parity rather
     * than repurposing it to read `activity`, which would change behaviour.
     */
    private fun evalOccupation(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        if (eligibility.occupation.isNullOrEmpty()) return null
        return null
    }

    private fun evalExistingBusiness(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        val rule = eligibility.existingBusiness ?: return null
        val existing = profile.existingBusiness
            ?: return RuleCheck(
                "existing_business", RuleStatus.MISSING,
                "Please confirm: do you have an existing business? This scheme requires ${if (rule) "an existing business" else "a new/upcoming business"}."
            )
        return if (existing == rule) {
            RuleCheck("existing_business", RuleStatus.PASS, "Business status (${if (existing) "existing" else "new"}) meets scheme requirement.")
        } else {
            RuleCheck(
                "existing_business", RuleStatus.FAIL,
                if (rule) "This scheme requires an already-operating business. New businesses are not eligible."
                else "This scheme is for new business start-ups. Existing businesses are not eligible."
            )
        }
    }

    private fun evalDisability(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        val rule = eligibility.disability ?: return null
        if (!rule) return null
        val disability = profile.disability
            ?: return RuleCheck("disability", RuleStatus.MISSING, "This scheme is for Persons with Disabilities (PwD). Please confirm disability status.")
        return if (disability) {
            RuleCheck("disability", RuleStatus.PASS, "PwD status confirmed - eligible for this scheme.")
        } else {
            RuleCheck("disability", RuleStatus.FAIL, "This scheme is exclusively for Persons with Disabilities (PwD).")
        }
    }

    private fun evalProjectCost(profile: BeneficiaryProfile, eligibility: Eligibility): RuleCheck? {
        val min = eligibility.minProjectCost
        val max = eligibility.maxProjectCost
        if (min == null && max == null) return null
        val cost = profile.projectCost
            ?: return RuleCheck("project_cost", RuleStatus.MISSING, "Project cost not provided - required to check financing eligibility.")
        val passMin = min == null || cost >= min
        val passMax = max == null || cost <= max
        return if (passMin && passMax) {
            RuleCheck("project_cost", RuleStatus.PASS, "Project cost ${rupees(cost)} is within eligible range.")
        } else {
            val minStr = min?.let { " (min: ${rupees(it)})" }.orEmpty()
            val maxStr = max?.let { " (max: ${rupees(it)})" }.orEmpty()
            RuleCheck("project_cost", RuleStatus.FAIL, "Project cost ${rupees(cost)} is outside eligible range$minStr$maxStr.")
        }
    }

    private fun evalCustomRules(profile: BeneficiaryProfile, eligibility: Eligibility): List<RuleCheck>? {
        if (eligibility.customRules.isEmpty()) return null
        val results = mutableListOf<RuleCheck>()
        for (rule in eligibility.customRules) {
            val fieldValue = profile.fieldValue(rule.field)
            if (fieldValue == null) {
                results += RuleCheck("custom_${rule.field}", RuleStatus.MISSING, rule.message.ifBlank { "Field \"${rule.field}\" is required for this scheme." })
                continue
            }
            if (evalCustomOperator(fieldValue, rule)) {
                results += RuleCheck("custom_${rule.field}", RuleStatus.PASS, rule.message.ifBlank { "Custom rule \"${rule.field} ${rule.operator} ${rule.value}\" passed." })
            } else {
                results += RuleCheck("custom_${rule.field}", RuleStatus.FAIL, rule.message.ifBlank { "Custom rule failed: ${rule.field} ${rule.operator} ${rule.value}." })
            }
        }
        return results.ifEmpty { null }
    }

    private fun evalCustomOperator(fieldValue: Any, rule: CustomRule): Boolean {
        val a = (fieldValue as? Number)?.toDouble()
        return when (rule.operator) {
            CustomOperator.GT -> a != null && (rule.value as? CustomRuleValue.Num)?.let { a > it.value } == true
            CustomOperator.GTE -> a != null && (rule.value as? CustomRuleValue.Num)?.let { a >= it.value } == true
            CustomOperator.LT -> a != null && (rule.value as? CustomRuleValue.Num)?.let { a < it.value } == true
            CustomOperator.LTE -> a != null && (rule.value as? CustomRuleValue.Num)?.let { a <= it.value } == true
            CustomOperator.EQ -> norm(fieldValue) == norm((rule.value as? CustomRuleValue.Str)?.value ?: rule.value)
            CustomOperator.NEQ -> norm(fieldValue) != norm((rule.value as? CustomRuleValue.Str)?.value ?: rule.value)
            CustomOperator.IN -> (rule.value as? CustomRuleValue.StrList)?.value?.any { norm(it) == norm(fieldValue) } == true
            CustomOperator.NOT_IN -> (rule.value as? CustomRuleValue.StrList)?.value?.none { norm(it) == norm(fieldValue) } == true
            CustomOperator.BETWEEN -> a != null && (rule.value as? CustomRuleValue.Range)?.let { a in it.min..it.max } == true
        }
    }

    override fun evaluateScheme(profile: BeneficiaryProfile, scheme: Scheme): EligibilityResult {
        val eligibility = scheme.eligibility

        val allResults = buildList {
            evalAge(profile, eligibility)?.let(::add)
            evalIncome(profile, eligibility)?.let(::add)
            evalGender(profile, eligibility)?.let(::add)
            evalSocialCategory(profile, eligibility)?.let(::add)
            evalActivity(profile, eligibility)?.let(::add)
            evalActivityCategory(profile, eligibility)?.let(::add)
            evalLocation(profile, eligibility)?.let(::addAll)
            evalOccupation(profile, eligibility)?.let(::add)
            evalExistingBusiness(profile, eligibility)?.let(::add)
            evalDisability(profile, eligibility)?.let(::add)
            evalProjectCost(profile, eligibility)?.let(::add)
            evalCustomRules(profile, eligibility)?.let(::addAll)
        }

        val passed = allResults.filter { it.status == RuleStatus.PASS }
        val failed = allResults.filter { it.status == RuleStatus.FAIL }
        val missing = allResults.filter { it.status == RuleStatus.MISSING }

        val status = when {
            failed.isNotEmpty() -> EligibilityStatus.NOT_ELIGIBLE
            missing.isNotEmpty() -> EligibilityStatus.NEEDS_MORE_INFO
            else -> EligibilityStatus.ELIGIBLE
        }

        val totalApplicable = passed.size + failed.size
        val confidence = if (totalApplicable == 0) 50 else Math.round((passed.size.toDouble() / totalApplicable) * 100).toInt()

        return EligibilityResult(
            schemeId = scheme.schemeId,
            status = status,
            passed = passed,
            failed = failed,
            missing = missing,
            confidence = confidence,
            totalRulesChecked = allResults.size
        )
    }

    override fun evaluateAllSchemes(profile: BeneficiaryProfile, schemes: List<Scheme>): EvaluatedSchemes {
        val eligible = mutableListOf<Pair<Scheme, EligibilityResult>>()
        val partial = mutableListOf<Pair<Scheme, EligibilityResult>>()
        val ineligible = mutableListOf<Pair<Scheme, EligibilityResult>>()

        for (scheme in schemes) {
            if (!scheme.metadata.active) continue
            val result = evaluateScheme(profile, scheme)
            when (result.status) {
                EligibilityStatus.ELIGIBLE -> eligible += scheme to result
                EligibilityStatus.NEEDS_MORE_INFO -> partial += scheme to result
                EligibilityStatus.NOT_ELIGIBLE -> ineligible += scheme to result
            }
        }

        return EvaluatedSchemes(eligible = eligible, partial = partial, ineligible = ineligible)
    }
}
