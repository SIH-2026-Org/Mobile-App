package com.setu.saarthi.domain.repository

import com.setu.saarthi.domain.model.Scheme
import kotlinx.coroutines.flow.Flow

/**
 * The externally-maintained scheme database. Backed today by
 * [com.setu.saarthi.data.repository.SchemeRepositoryImpl] over
 * [com.setu.saarthi.data.mock.MockSchemeData]; swap the impl for a real
 * ministry/aggregator API later without touching any call site.
 */
interface SchemeRepository {
    suspend fun getAll(): Result<List<Scheme>>
    suspend fun getById(id: String): Result<Scheme>
    fun observeAll(): Flow<List<Scheme>>
}
