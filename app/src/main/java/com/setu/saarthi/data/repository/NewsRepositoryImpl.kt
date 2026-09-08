package com.setu.saarthi.data.repository

import com.setu.saarthi.data.mock.MockNewsData
import com.setu.saarthi.domain.model.NewsItem
import com.setu.saarthi.domain.repository.NewsRepository
import kotlinx.coroutines.delay

class NewsRepositoryImpl : NewsRepository {
    override suspend fun getLatest(): Result<List<NewsItem>> {
        delay(300)
        return Result.success(MockNewsData.news)
    }
}
