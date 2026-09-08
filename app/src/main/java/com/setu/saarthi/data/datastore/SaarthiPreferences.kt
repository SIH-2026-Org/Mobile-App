package com.setu.saarthi.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.saarthiDataStore: DataStore<Preferences> by preferencesDataStore(name = "saarthi_setu_prefs")

object PreferenceKeys {
    val PROFILE_NAME = stringPreferencesKey("profile_name")
    val PROFILE_AGE = intPreferencesKey("profile_age")
    val PROFILE_GENDER = stringPreferencesKey("profile_gender")
    val PROFILE_STATE = stringPreferencesKey("profile_state")
    val PROFILE_DISTRICT = stringPreferencesKey("profile_district")
    val PROFILE_AREA_TYPE = stringPreferencesKey("profile_area_type")
    val PROFILE_ACTIVITY = stringPreferencesKey("profile_activity")
    val PROFILE_ACTIVITY_CATEGORY = stringPreferencesKey("profile_activity_category")
    val PROFILE_ANNUAL_INCOME = longPreferencesKey("profile_annual_income")
    val PROFILE_SOCIAL_CATEGORY = stringPreferencesKey("profile_social_category")
    val PROFILE_DISABILITY = booleanPreferencesKey("profile_disability")
    val PROFILE_HAS_EXISTING_BUSINESS = booleanPreferencesKey("profile_has_existing_business")
    val PROFILE_ONBOARDING_COMPLETE = booleanPreferencesKey("profile_onboarding_complete")

    val SAVED_SCHEME_IDS = stringSetPreferencesKey("saved_scheme_ids")
}
