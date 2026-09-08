package com.setu.saarthi.domain.model

/**
 * Multilingual scaffold — only EN/HI exist today and neither is surfaced in
 * the UI yet, but [com.setu.saarthi.domain.future.LanguageRepository] and the
 * mock extractor already key off this enum so a real language switcher can be
 * added later without touching the rest of the app.
 */
enum class Language(val displayName: String) {
    ENGLISH("English"),
    HINDI("हिन्दी")
}
