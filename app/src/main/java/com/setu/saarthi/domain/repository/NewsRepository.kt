package com.setu.saarthi.domain.repository

import com.setu.saarthi.domain.model.NewsItem

/** Government scheme news/announcements shown on the Home feed. */
interface NewsRepository {
    suspend fun getLatest(): Result<List<NewsItem>>
}
