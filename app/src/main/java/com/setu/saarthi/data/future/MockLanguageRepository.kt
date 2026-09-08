package com.setu.saarthi.data.future

import com.setu.saarthi.domain.future.LanguageRepository
import com.setu.saarthi.domain.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow

class MockLanguageRepository : LanguageRepository {
    private val current = MutableStateFlow(Language.ENGLISH)

    override fun observeCurrentLanguage(): Flow<Language> = current.asStateFlow()
    override fun availableLanguages(): List<Language> = Language.entries
    override suspend fun setLanguage(language: Language) {
        current.value = language
    }
}
