package com.setu.saarthi.data.repository

import android.content.Context
import com.setu.saarthi.data.datastore.PreferenceKeys
import com.setu.saarthi.data.datastore.saarthiDataStore
import com.setu.saarthi.domain.model.AreaType
import com.setu.saarthi.domain.model.Gender
import com.setu.saarthi.domain.model.SocialCategory
import com.setu.saarthi.domain.model.UserProfile
import com.setu.saarthi.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit

class ProfileRepositoryImpl(private val context: Context) : ProfileRepository {

    override fun observeProfile(): Flow<UserProfile> = context.saarthiDataStore.data.map { prefs ->
        UserProfile(
            name = prefs[PreferenceKeys.PROFILE_NAME].orEmpty(),
            age = prefs[PreferenceKeys.PROFILE_AGE],
            gender = prefs[PreferenceKeys.PROFILE_GENDER]?.let { runCatching { Gender.valueOf(it) }.getOrNull() },
            state = prefs[PreferenceKeys.PROFILE_STATE],
            district = prefs[PreferenceKeys.PROFILE_DISTRICT],
            areaType = prefs[PreferenceKeys.PROFILE_AREA_TYPE]?.let { runCatching { AreaType.valueOf(it) }.getOrNull() },
            activity = prefs[PreferenceKeys.PROFILE_ACTIVITY],
            activityCategory = prefs[PreferenceKeys.PROFILE_ACTIVITY_CATEGORY],
            annualIncome = prefs[PreferenceKeys.PROFILE_ANNUAL_INCOME],
            socialCategory = prefs[PreferenceKeys.PROFILE_SOCIAL_CATEGORY]?.let { runCatching { SocialCategory.valueOf(it) }.getOrNull() },
            disability = prefs[PreferenceKeys.PROFILE_DISABILITY],
            hasExistingBusiness = prefs[PreferenceKeys.PROFILE_HAS_EXISTING_BUSINESS],
            onboardingComplete = prefs[PreferenceKeys.PROFILE_ONBOARDING_COMPLETE] ?: false
        )
    }

    override suspend fun saveProfile(profile: UserProfile) {
        context.saarthiDataStore.edit { prefs ->
            prefs[PreferenceKeys.PROFILE_NAME] = profile.name
            profile.age?.let { prefs[PreferenceKeys.PROFILE_AGE] = it }
            profile.gender?.let { prefs[PreferenceKeys.PROFILE_GENDER] = it.name }
            profile.state?.let { prefs[PreferenceKeys.PROFILE_STATE] = it }
            profile.district?.let { prefs[PreferenceKeys.PROFILE_DISTRICT] = it }
            profile.areaType?.let { prefs[PreferenceKeys.PROFILE_AREA_TYPE] = it.name }
            profile.activity?.let { prefs[PreferenceKeys.PROFILE_ACTIVITY] = it }
            profile.activityCategory?.let { prefs[PreferenceKeys.PROFILE_ACTIVITY_CATEGORY] = it }
            profile.annualIncome?.let { prefs[PreferenceKeys.PROFILE_ANNUAL_INCOME] = it }
            profile.socialCategory?.let { prefs[PreferenceKeys.PROFILE_SOCIAL_CATEGORY] = it.name }
            profile.disability?.let { prefs[PreferenceKeys.PROFILE_DISABILITY] = it }
            profile.hasExistingBusiness?.let { prefs[PreferenceKeys.PROFILE_HAS_EXISTING_BUSINESS] = it }
            prefs[PreferenceKeys.PROFILE_ONBOARDING_COMPLETE] = profile.onboardingComplete
        }
    }
}
