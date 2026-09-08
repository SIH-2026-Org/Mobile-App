package com.setu.saarthi.domain.llm

import com.setu.saarthi.domain.model.BeneficiaryProfile

/**
 * THE LLM LAYER. Turns a free-text query (Hindi, English, or Hinglish) into a
 * partial [BeneficiaryProfile] - only the fields it could find in the text
 * are non-null. [com.setu.saarthi.data.llm.MockRequirementExtractor] is a
 * deterministic keyword/regex parser standing in for this today, ported
 * from the reference engine's `profile.builder.js#extractFromText`, which
 * itself is documented there as "a fallback / lightweight parser for the
 * prototype... would be replaced by Sarvam AI NLU / an LLM in production" -
 * the signature here is shaped so a real LLM call is a drop-in replacement.
 */
interface RequirementExtractor {
    suspend fun extract(text: String): Result<BeneficiaryProfile>
}
