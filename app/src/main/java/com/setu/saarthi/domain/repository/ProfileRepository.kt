package com.setu.saarthi.domain.repository

import com.setu.saarthi.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/** The onboarding profile, DataStore-backed today. */
interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun saveProfile(profile: UserProfile)
}
