package com.coldblue.data.repo

import com.coldblue.data.mapper.Mapper.asEntity
import com.coldblue.data.mapper.Mapper.asDomain
import com.coldblue.database.dao.MandaKeyDao
import com.coldblue.model.MandaKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MandaKeyRepositoryImpl @Inject constructor(
    private val mandaKeyDao: MandaKeyDao,
) : MandaKeyRepository {
    override fun getMandaKeys(): Flow<List<MandaKey>> =
        mandaKeyDao.getMandaKeys().map { it.map { it.asDomain() } }

    override suspend fun upsertMandaKeys(mandaKeys: List<MandaKey>) {
        mandaKeyDao.upsertMandaKeys(mandaKeys.map { it.asEntity() })
    }
}