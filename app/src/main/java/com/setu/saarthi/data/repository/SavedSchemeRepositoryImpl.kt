package com.setu.saarthi.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.setu.saarthi.data.datastore.PreferenceKeys
import com.setu.saarthi.data.datastore.saarthiDataStore
import com.setu.saarthi.domain.repository.SavedSchemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedSchemeRepositoryImpl(private val context: Context) : SavedSchemeRepository {

    override fun observeSavedIds(): Flow<Set<String>> = context.saarthiDataStore.data.map { prefs ->
        prefs[PreferenceKeys.SAVED_SCHEME_IDS] ?: emptySet()
    }

    override suspend fun toggleSaved(schemeId: String) {
        context.saarthiDataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.SAVED_SCHEME_IDS] ?: emptySet()
            prefs[PreferenceKeys.SAVED_SCHEME_IDS] = if (schemeId in current) current - schemeId else current + schemeId
        }
    }
}
