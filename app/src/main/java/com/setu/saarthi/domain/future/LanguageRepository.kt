package com.setu.saarthi.domain.future

import com.setu.saarthi.domain.model.Language
import kotlinx.coroutines.flow.Flow

/**
 * Multilingual support scaffold. Wired into DI but not surfaced in the UI
 * yet — a future language switcher would read/write this.
 */
interface LanguageRepository {
    fun observeCurrentLanguage(): Flow<Language>
    fun availableLanguages(): List<Language>
    suspend fun setLanguage(language: Language)
}
