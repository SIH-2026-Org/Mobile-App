package com.setu.saarthi.data.engine

import com.setu.saarthi.domain.engine.DocumentChecklistGenerator
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.ChecklistDocument
import com.setu.saarthi.domain.model.DocumentChecklist
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.model.SchemeDocument
import com.setu.saarthi.domain.model.SocialCategory

/** Ported from the reference engine's `document.generator.js`. */
class DefaultDocumentChecklistGenerator : DocumentChecklistGenerator {

    private companion object {
        val COMMONLY_AVAILABLE = setOf(
            "aadhaar", "pan", "photo", "bank_passbook", "bank_account",
            "ration_card", "voter_id", "mobile_number"
        )

        val OBTAIN_GUIDANCE = mapOf(
            "income_cert" to "Obtain from Tehsildar / Sub-Divisional Magistrate office.",
            "category_cert" to "SC/ST/OBC certificate from District Social Welfare Office.",
            "caste_cert" to "Caste certificate from District Social Welfare Office.",
            "business_plan" to "Prepare a brief business plan (1-2 pages) with cost estimates.",
            "project_report" to "Detailed Project Report (DPR) from a bank-empanelled consultant.",
            "quotation" to "Obtain quotation/invoice from machinery or equipment supplier.",
            "land_doc" to "Land record (Khasra/Patta) from Patwari / Revenue department.",
            "lease_agreement" to "Registered shop/land lease agreement from lessor.",
            "trade_license" to "Trade/business license from local municipal body (Nagar Panchayat/Palika).",
            "gst_cert" to "GST registration certificate from GST portal (if applicable).",
            "udyam_reg" to "Udyam registration from udyamregistration.gov.in (free, instant).",
            "edu_certificate" to "Educational qualification certificate from school/college.",
            "disability_cert" to "Disability certificate from Chief Medical Officer (CMO).",
            "minority_cert" to "Self-declaration affidavit or community certificate for minority status.",
            "age_proof" to "Birth certificate / Aadhaar / school leaving certificate.",
            "address_proof" to "Utility bill / rental agreement / Aadhaar with current address.",
            "experience_cert" to "Experience letter from previous employer or self-declaration.",
            "training_cert" to "Skill training completion certificate from recognized institute.",
            "survey_letter" to "Urban local body survey letter for street vendors (PM-SVANidhi).",
            "vendor_id" to "Vendor identification certificate from Urban Local Body.",
            "affidavit" to "Notarized self-declaration affidavit (available from any notary).",
            "bank_statement" to "6-month bank account statement from your bank branch."
        )
    }

    private enum class Tier { AVAILABLE, OBTAIN, REQUIRED }

    private fun classify(doc: SchemeDocument, profile: BeneficiaryProfile): Tier {
        val id = doc.id.lowercase()

        if (id in COMMONLY_AVAILABLE) return Tier.AVAILABLE

        if (id == "category_cert" || id == "caste_cert") {
            return if (profile.socialCategory != null && profile.socialCategory != SocialCategory.GEN) {
                Tier.OBTAIN
            } else if (doc.required) Tier.OBTAIN else Tier.AVAILABLE
        }

        if (id == "disability_cert") {
            return if (profile.disability == true) Tier.OBTAIN else Tier.AVAILABLE
        }

        if (id == "minority_cert") {
            return if (profile.socialCategory == SocialCategory.MINORITY) Tier.OBTAIN else Tier.AVAILABLE
        }

        if (OBTAIN_GUIDANCE.containsKey(id)) return Tier.OBTAIN

        return if (doc.required) Tier.REQUIRED else Tier.OBTAIN
    }

    override fun generateChecklist(profile: BeneficiaryProfile, scheme: Scheme): DocumentChecklist {
        val available = mutableListOf<ChecklistDocument>()
        val obtain = mutableListOf<ChecklistDocument>()
        val required = mutableListOf<ChecklistDocument>()

        for (doc in scheme.documents) {
            val entry = ChecklistDocument(
                id = doc.id,
                name = doc.name,
                required = doc.required,
                note = doc.note ?: OBTAIN_GUIDANCE[doc.id.lowercase()]
            )
            when (classify(doc, profile)) {
                Tier.AVAILABLE -> available += entry
                Tier.OBTAIN -> obtain += entry
                Tier.REQUIRED -> required += entry
            }
        }

        val mandatoryTotal = scheme.documents.count { it.required }
        val mandatoryAvailable = available.count { it.required }
        val readinessPct = if (mandatoryTotal > 0) {
            Math.round((mandatoryAvailable.toDouble() / mandatoryTotal) * 100).toInt()
        } else 100

        return DocumentChecklist(
            schemeId = scheme.schemeId,
            schemeName = scheme.shortName,
            readinessPct = readinessPct,
            totalDocuments = scheme.documents.size,
            available = available,
            obtain = obtain,
            required = required
        )
    }
}
