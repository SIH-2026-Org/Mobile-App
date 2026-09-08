package com.setu.saarthi.data.future

import com.setu.saarthi.domain.future.ApplicationHandoffService
import com.setu.saarthi.domain.future.HandoffPacket
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.model.UserProfile
import kotlinx.coroutines.delay

/** TODO: replace with a real government-portal handoff / pre-fill API. */
class MockApplicationHandoffService : ApplicationHandoffService {
    override suspend fun prepare(scheme: Scheme, profile: UserProfile): Result<HandoffPacket> {
        delay(300)
        val fields = buildMap {
            put("name", profile.name)
            profile.age?.let { put("age", it.toString()) }
            profile.state?.let { put("state", it) }
            profile.activity?.let { put("activity", it) }
        }
        return Result.success(HandoffPacket(schemeId = scheme.schemeId, prefilledFields = fields, handoffUrl = scheme.metadata.officialUrl))
    }
}
