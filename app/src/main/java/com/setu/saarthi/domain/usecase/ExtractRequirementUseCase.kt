package com.setu.saarthi.domain.usecase

import com.setu.saarthi.domain.llm.RequirementExtractor
import com.setu.saarthi.domain.model.BeneficiaryProfile

/** The "User Text -> LLM Layer -> Structured Requirements" step. */
class ExtractRequirementUseCase(private val extractor: RequirementExtractor) {
    suspend operator fun invoke(text: String): Result<BeneficiaryProfile> = extractor.extract(text)
}
