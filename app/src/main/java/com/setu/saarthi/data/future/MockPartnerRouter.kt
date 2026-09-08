package com.setu.saarthi.data.future

import com.setu.saarthi.domain.future.Partner
import com.setu.saarthi.domain.future.PartnerRouter
import com.setu.saarthi.domain.model.Scheme
import kotlinx.coroutines.delay

/** Canned partner list. TODO: replace with a real bank/NBFC/CSC directory API. */
class MockPartnerRouter : PartnerRouter {
    override suspend fun findPartners(scheme: Scheme, state: String?): Result<List<Partner>> {
        delay(300)
        val partners = scheme.channelPartners.types.mapIndexed { index, type ->
            Partner(
                id = "${scheme.schemeId}_partner_$index",
                name = "$type ${state ?: "Branch"} Office",
                type = type,
                contactInfo = "Contact your nearest $type branch"
            )
        }
        return Result.success(partners)
    }
}
