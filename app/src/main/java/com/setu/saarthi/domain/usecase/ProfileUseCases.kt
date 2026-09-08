package com.setu.saarthi.domain.usecase

import com.setu.saarthi.domain.model.UserProfile
import com.setu.saarthi.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveProfileUseCase(private val profileRepository: ProfileRepository) {
    operator fun invoke(): Flow<UserProfile> = profileRepository.observeProfile()
}

class SaveProfileUseCase(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(profile: UserProfile) = profileRepository.saveProfile(profile)
}
