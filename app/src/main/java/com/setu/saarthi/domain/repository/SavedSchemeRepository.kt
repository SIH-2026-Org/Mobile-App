package com.setu.saarthi.domain.repository

import kotlinx.coroutines.flow.Flow

/** Bookmarked scheme ids, DataStore-backed today. */
interface SavedSchemeRepository {
    fun observeSavedIds(): Flow<Set<String>>
    suspend fun toggleSaved(schemeId: String)
}
