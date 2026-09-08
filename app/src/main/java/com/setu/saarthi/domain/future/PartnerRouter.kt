package com.setu.saarthi.domain.future

import com.setu.saarthi.domain.model.Scheme

/** A bank/NBFC/CSC partner that can process a scheme application in a given state. */
data class Partner(
    val id: String,
    val name: String,
    val type: String,
    val contactInfo: String
)

/**
 * Partner routing scaffold — connecting a user to a bank/NBFC/CSC that can
 * process their chosen scheme. Wired into DI but not surfaced in the UI yet.
 */
interface PartnerRouter {
    suspend fun findPartners(scheme: Scheme, state: String?): Result<List<Partner>>
}
