package com.setu.saarthi.data.llm

import com.setu.saarthi.domain.llm.RequirementExtractor
import com.setu.saarthi.domain.model.AreaType
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.Gender
import com.setu.saarthi.domain.model.SocialCategory
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Regex-based entity extractor, ported from the reference engine's
 * `services/profile.builder.js#extractFromText()`. That file's own
 * docstring frames it as "a fallback / lightweight parser for the
 * prototype... in production this would be replaced / augmented by
 * Sarvam AI NLU" - i.e. it already IS the mock-LLM stand-in the app
 * needs, just written in JS. This is a line-for-line Kotlin port of its
 * pattern tables and extraction logic.
 *
 * The artificial [PARSE_DELAY_MS] delay keeps the Home screen's loading
 * state visible for a demo; a real extractor would have genuine network
 * latency here instead.
 */
class MockRequirementExtractor : RequirementExtractor {

    private data class ActivityPattern(val pattern: Regex, val activity: String, val category: String)
    private data class AmountPattern(val pattern: Regex, val multiplier: Double, val clean: Boolean = false)

    private companion object {
        const val PARSE_DELAY_MS = 900L
        const val FORCE_ERROR_TRIGGER = "__force_error__"

        val ACTIVITY_PATTERNS = listOf(
            ActivityPattern(Regex("dairy|milk|cow|buffalo|cattle|gau|dugdh", RegexOption.IGNORE_CASE), "dairy", "agriculture"),
            ActivityPattern(Regex("poultry|chicken|broiler|layer|egg|murgi", RegexOption.IGNORE_CASE), "poultry", "agriculture"),
            ActivityPattern(Regex("goat|sheep|bakri|pashu|livestock|animal", RegexOption.IGNORE_CASE), "animal_husbandry", "agriculture"),
            ActivityPattern(Regex("fish|fishery|fisheries|aquaculture|machli", RegexOption.IGNORE_CASE), "fishery", "agriculture"),
            ActivityPattern(Regex("farm|crop|khet|kheti|kisaan|kisan|agriculture", RegexOption.IGNORE_CASE), "agriculture", "agriculture"),
            ActivityPattern(Regex("street.?vend|rehdi|thela|hawker|footpath|vendor", RegexOption.IGNORE_CASE), "street_vending", "services"),
            ActivityPattern(Regex("tailor|sewing|stitch|kapda|garment|cloth", RegexOption.IGNORE_CASE), "tailoring", "msme"),
            ActivityPattern(Regex("weav|handloom|loom|bunkar|khaddar", RegexOption.IGNORE_CASE), "weaving", "msme"),
            ActivityPattern(Regex("food.?process|pickle|papad|snack|bakery|catering", RegexOption.IGNORE_CASE), "food_processing", "msme"),
            ActivityPattern(Regex("restaurant|dhaba|hotel|canteen|tiffin", RegexOption.IGNORE_CASE), "food_services", "services"),
            ActivityPattern(Regex("beauty|salon|parlour|parlor|barber", RegexOption.IGNORE_CASE), "beauty_services", "services"),
            ActivityPattern(Regex("transport|taxi|auto|rickshaw|logistics|truck", RegexOption.IGNORE_CASE), "transport", "services"),
            ActivityPattern(Regex("electronic|mobile.?repair|computer|laptop", RegexOption.IGNORE_CASE), "electronics", "msme"),
            ActivityPattern(Regex("manufactur|production|factory|unit", RegexOption.IGNORE_CASE), "manufacturing", "msme"),
            ActivityPattern(Regex("shop|dukan|retail|store|trader", RegexOption.IGNORE_CASE), "retail", "msme"),
            ActivityPattern(Regex("handicraft|craft|artisan|pottery|sculpture", RegexOption.IGNORE_CASE), "handicraft", "msme"),
            ActivityPattern(Regex("construct|carpenter|plumber|mason|electric", RegexOption.IGNORE_CASE), "construction", "services"),
            ActivityPattern(Regex("education|tuition|coaching|school|teaching", RegexOption.IGNORE_CASE), "education", "education"),
            ActivityPattern(Regex("pharmacy|medical|clinic|health|doctor", RegexOption.IGNORE_CASE), "health_services", "services"),
            ActivityPattern(Regex("tech|software|it|digital|online", RegexOption.IGNORE_CASE), "technology", "services"),
            ActivityPattern(Regex("solar|renewable|energy", RegexOption.IGNORE_CASE), "energy", "agriculture"),
            ActivityPattern(Regex("msme|small.?business|micro|enterprise", RegexOption.IGNORE_CASE), "msme", "msme")
        )

        val AMOUNT_PATTERNS = listOf(
            AmountPattern(Regex("(\\d+(?:\\.\\d+)?)\\s*(?:lakh|lac|lacs|lakhs)", RegexOption.IGNORE_CASE), 100_000.0),
            AmountPattern(Regex("(\\d+(?:\\.\\d+)?)\\s*crore", RegexOption.IGNORE_CASE), 10_000_000.0),
            AmountPattern(Regex("(\\d+(?:\\.\\d+)?)\\s*(?:thousand|hazar)", RegexOption.IGNORE_CASE), 1_000.0),
            AmountPattern(Regex("(?:rs\\.?|₹|inr)?\\s*([1-9]\\d{3,}(?:[,\\d]*)?)", RegexOption.IGNORE_CASE), 1.0, clean = true)
        )

        val INDIAN_STATES = listOf(
            "andhra pradesh", "arunachal pradesh", "assam", "bihar", "chhattisgarh",
            "goa", "gujarat", "haryana", "himachal pradesh", "jharkhand", "karnataka",
            "kerala", "madhya pradesh", "maharashtra", "manipur", "meghalaya", "mizoram",
            "nagaland", "odisha", "punjab", "rajasthan", "sikkim", "tamil nadu",
            "telangana", "tripura", "uttar pradesh", "uttarakhand", "west bengal",
            "delhi", "jammu and kashmir", "ladakh", "chandigarh", "puducherry",
            "andaman", "lakshadweep", "dadra", "daman"
        )

        val STATE_ABBREVIATIONS = mapOf(
            "up" to "Uttar Pradesh", "mp" to "Madhya Pradesh", "wb" to "West Bengal",
            "ap" to "Andhra Pradesh", "hp" to "Himachal Pradesh", "jk" to "Jammu and Kashmir",
            "uk" to "Uttarakhand", "hr" to "Haryana", "pb" to "Punjab"
        )
    }

    private fun extractAmounts(text: String): List<Long> {
        val amounts = mutableListOf<Long>()
        for (p in AMOUNT_PATTERNS) {
            val match = p.pattern.find(text) ?: continue
            var numStr = match.groupValues[1]
            if (p.clean) numStr = numStr.replace(",", "")
            val value = numStr.toDoubleOrNull()?.times(p.multiplier) ?: continue
            if (value > 0) amounts += Math.round(value)
        }
        return amounts
    }

    private fun extractActivity(text: String): Pair<String, String>? {
        for (p in ACTIVITY_PATTERNS) {
            if (p.pattern.containsMatchIn(text)) return p.activity to p.category
        }
        return null
    }

    private fun extractState(text: String): String? {
        val lower = text.lowercase(Locale.ROOT)
        for (state in INDIAN_STATES) {
            if (lower.contains(state)) {
                return state.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.titlecase(Locale.ROOT) } }
            }
        }
        for ((short, full) in STATE_ABBREVIATIONS) {
            if (Regex("\\b$short\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) return full
        }
        return null
    }

    private fun extractSocialCategory(text: String): SocialCategory? {
        val t = text.uppercase(Locale.ROOT)
        return when {
            Regex("\\bSC\\b|SCHEDULED.?CASTE|DALIT", RegexOption.IGNORE_CASE).containsMatchIn(t) -> SocialCategory.SC
            Regex("\\bST\\b|SCHEDULED.?TRIBE|ADIVASI|TRIBAL", RegexOption.IGNORE_CASE).containsMatchIn(t) -> SocialCategory.ST
            Regex("\\bOBC\\b|OTHER.?BACKWARD", RegexOption.IGNORE_CASE).containsMatchIn(t) -> SocialCategory.OBC
            Regex("\\bEWS\\b|ECONOMICALLY.?WEAKER", RegexOption.IGNORE_CASE).containsMatchIn(t) -> SocialCategory.EWS
            Regex("MINORITY|MUSLIM|CHRISTIAN|SIKH|BUDDHIST|JAIN", RegexOption.IGNORE_CASE).containsMatchIn(t) -> SocialCategory.MINORITY
            Regex("GENERAL|GEN\\b|OPEN\\b", RegexOption.IGNORE_CASE).containsMatchIn(t) -> SocialCategory.GEN
            else -> null
        }
    }

    private fun extractGender(text: String): Gender? = when {
        Regex("\\bfemale\\b|\\bwoman\\b|\\bwomen\\b", RegexOption.IGNORE_CASE).containsMatchIn(text) -> Gender.FEMALE
        Regex("\\bmale\\b|\\bman\\b|\\bpurush\\b", RegexOption.IGNORE_CASE).containsMatchIn(text) -> Gender.MALE
        Regex("\\bother\\b|\\btransgender\\b|\\bthird.?gender", RegexOption.IGNORE_CASE).containsMatchIn(text) -> Gender.OTHER
        else -> null
    }

    private fun extractAge(text: String): Int? {
        val match = Regex("(\\d{2})\\s*(?:year|yr|saal|sal|age)", RegexOption.IGNORE_CASE).find(text)
            ?: Regex("age[:\\s]+(\\d{2})", RegexOption.IGNORE_CASE).find(text)
            ?: Regex("i\\s+am\\s+(\\d{2})", RegexOption.IGNORE_CASE).find(text)
            ?: return null
        val age = match.groupValues[1].toIntOrNull() ?: return null
        return if (age in 16..75) age else null
    }

    private fun extractPurpose(text: String): String? = when {
        Regex("start|new|open|launch|shuru|naya", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "starting_business"
        Regex("expand|grow|upgrade|badhana|vishtar", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "expansion"
        Regex("working.?capital|stock|inventory|raqam", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "working_capital"
        Regex("equipment|machine|tool|yantra", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "equipment_purchase"
        Regex("education|study|college|school", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "education"
        Regex("house|home|ghar|awas", RegexOption.IGNORE_CASE).containsMatchIn(text) -> "housing"
        else -> null
    }

    /** Direct port of `extractFromText()` - returns a partial [BeneficiaryProfile] with only the fields found. */
    private fun extractFromText(text: String): BeneficiaryProfile {
        val amounts = extractAmounts(text)
        val (projectCost, loanRequired) = if (amounts.isNotEmpty()) {
            val sorted = amounts.sortedDescending()
            sorted.first() to sorted.last()
        } else null to null

        val activityInfo = extractActivity(text)

        var areaType: AreaType? = null
        if (Regex("\\burban\\b|city|town|nagar|sheher", RegexOption.IGNORE_CASE).containsMatchIn(text)) areaType = AreaType.URBAN
        if (Regex("\\brural\\b|village|gaon|gram\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) areaType = AreaType.RURAL

        var existingBusiness: Boolean? = null
        if (Regex("existing|already|pehle se|running|established", RegexOption.IGNORE_CASE).containsMatchIn(text)) existingBusiness = true
        if (Regex("new business|start.?up|shuru karna|naya", RegexOption.IGNORE_CASE).containsMatchIn(text)) existingBusiness = false

        return BeneficiaryProfile(
            age = extractAge(text),
            gender = extractGender(text),
            socialCategory = extractSocialCategory(text),
            state = extractState(text),
            areaType = areaType,
            activity = activityInfo?.first,
            activityCategory = activityInfo?.second,
            existingBusiness = existingBusiness,
            projectCost = projectCost,
            loanRequired = loanRequired,
            purpose = extractPurpose(text)
        )
    }

    override suspend fun extract(text: String): Result<BeneficiaryProfile> {
        delay(PARSE_DELAY_MS)

        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Please describe what you need help with."))
        }
        if (text.contains(FORCE_ERROR_TRIGGER, ignoreCase = true)) {
            return Result.failure(IllegalStateException("Could not understand the request. Please try rephrasing."))
        }

        return Result.success(extractFromText(text))
    }
}
