package com.setu.saarthi.domain.usecase

import com.setu.saarthi.domain.model.NewsItem
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.repository.NewsRepository
import com.setu.saarthi.domain.repository.SavedSchemeRepository
import com.setu.saarthi.domain.repository.SchemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetNewsFeedUseCase(private val newsRepository: NewsRepository) {
    suspend operator fun invoke(): Result<List<NewsItem>> = newsRepository.getLatest()
}

class GetSchemeDetailUseCase(private val schemeRepository: SchemeRepository) {
    suspend operator fun invoke(schemeId: String): Result<Scheme> = schemeRepository.getById(schemeId)
}

class ToggleSavedSchemeUseCase(private val savedSchemeRepository: SavedSchemeRepository) {
    suspend operator fun invoke(schemeId: String) = savedSchemeRepository.toggleSaved(schemeId)
}

class ObserveSavedIdsUseCase(private val savedSchemeRepository: SavedSchemeRepository) {
    operator fun invoke(): Flow<Set<String>> = savedSchemeRepository.observeSavedIds()
}

class GetSavedSchemesUseCase(
    private val savedSchemeRepository: SavedSchemeRepository,
    private val schemeRepository: SchemeRepository
) {
    operator fun invoke(): Flow<List<Scheme>> = combine(
        savedSchemeRepository.observeSavedIds(),
        schemeRepository.observeAll()
    ) { savedIds, schemes ->
        schemes.filter { it.schemeId in savedIds }
    }
}
