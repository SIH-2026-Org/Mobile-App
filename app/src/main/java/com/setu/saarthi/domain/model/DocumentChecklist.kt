package com.setu.saarthi.domain.model

/** Personalised document readiness entry, ported from `document.generator.js`. */
data class ChecklistDocument(val id: String, val name: String, val required: Boolean, val note: String?)

data class DocumentChecklist(
    val schemeId: String,
    val schemeName: String,
    /** 0..100 - share of mandatory documents already likely available. */
    val readinessPct: Int,
    val totalDocuments: Int,
    val available: List<ChecklistDocument>,
    val obtain: List<ChecklistDocument>,
    val required: List<ChecklistDocument>
)
