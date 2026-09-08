package com.setu.saarthi.domain.engine

import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.DocumentChecklist
import com.setu.saarthi.domain.model.Scheme

/**
 * Personalised document-readiness checklist, ported from
 * `document.generator.js`. Classifies each required document into
 * already-available / needs-to-be-obtained / mandatory-unknown tiers.
 */
interface DocumentChecklistGenerator {
    fun generateChecklist(profile: BeneficiaryProfile, scheme: Scheme): DocumentChecklist
}
