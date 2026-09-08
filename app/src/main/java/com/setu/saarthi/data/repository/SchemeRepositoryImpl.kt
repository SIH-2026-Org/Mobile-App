package com.setu.saarthi.data.repository

import com.setu.saarthi.data.mock.MockSchemeData
import com.setu.saarthi.domain.model.Scheme
import com.setu.saarthi.domain.repository.SchemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SchemeRepositoryImpl : SchemeRepository {

    private val schemesFlow = MutableStateFlow(MockSchemeData.schemes)

    override suspend fun getAll(): Result<List<Scheme>> = Result.success(schemesFlow.value)

    override suspend fun getById(id: String): Result<Scheme> {
        val scheme = schemesFlow.value.find { it.schemeId == id }
            ?: return Result.failure(NoSuchElementException("No scheme with id \"$id\""))
        return Result.success(scheme)
    }

    override fun observeAll(): Flow<List<Scheme>> = schemesFlow.asStateFlow()
}
