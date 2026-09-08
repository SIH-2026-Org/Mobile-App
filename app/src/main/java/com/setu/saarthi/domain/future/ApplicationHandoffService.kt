package com.setu.saarthi.domain.future

import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.model.UserProfile

/** A pre-filled packet ready to be handed off to an official application portal. */
data class HandoffPacket(
    val schemeId: String,
    val prefilledFields: Map<String, String>,
    val handoffUrl: String
)

/**
 * Official application handoff scaffold — preparing a pre-filled packet to
 * send the user to the real government portal. Wired into DI but not
 * surfaced in the UI yet.
 */
interface ApplicationHandoffService {
    suspend fun prepare(scheme: Scheme, profile: UserProfile): Result<HandoffPacket>
}
