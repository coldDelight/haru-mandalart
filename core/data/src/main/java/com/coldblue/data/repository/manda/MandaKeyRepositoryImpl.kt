package com.coldblue.data.repository.manda

import com.coldblue.data.mapper.MandaKeyMapper.asDomain
import com.coldblue.data.mapper.MandaKeyMapper.asEntity
import com.coldblue.data.mapper.MandaKeyMapper.asNetworkModel
import com.coldblue.data.mapper.MandaKeyMapper.asSyncedEntity
import com.coldblue.data.sync.SyncHelper
import com.coldblue.database.dao.MandaKeyDao
import com.coldblue.datastore.UpdateTimeDataSource
import com.coldblue.datastore.UserDataSource
import com.coldblue.model.MandaKey
import com.coldblue.network.datasource.MandaKeyDataSource
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MandaKeyRepositoryImpl @Inject constructor(
    private val mandaKeyDao: MandaKeyDao,
    private val mandaKeyDataSource: MandaKeyDataSource,
    private val syncHelper: SyncHelper,
    private val updateTimeDataSource: UpdateTimeDataSource,
    ) : MandaKeyRepository {
    override fun getMandaKeys(): Flow<List<MandaKey>> =
        mandaKeyDao.getMandaKeys().map { it.filter { !it.isDel }.map { it.asDomain() } }

    override suspend fun upsertMandaKeys(mandaKeys: List<MandaKey>) {
        mandaKeyDao.upsertMandaKeys(mandaKeys.map { it.asEntity() })
        syncHelper.syncWrite()
    }

    override suspend fun deleteMandaKeys(keyIdList: List<Int>, detailIdList: List<Int>) {
        Logger.d("$keyIdList\n$detailIdList")
        mandaKeyDao.deleteMandaKeyAndDetail(keyIdList, detailIdList)
    }

    override suspend fun deleteAllMandaDetail() {
        mandaKeyDao.deleteAllMandaKey()
    }

    override fun isInit(): Flow<Boolean> {
        return mandaKeyDao.getFinalManda().map { it != null }
    }

    override suspend fun syncRead(): Boolean {
        try {
            val remoteNew =
                mandaKeyDataSource.getMandaKey(updateTimeDataSource.mandaKeyUpdateTime.first())
            val originIds = remoteNew.map { it.id }
            // TODO 이름 바꿔야 함
            val todoIds = mandaKeyDao.getMandaKeyIdByOriginIds(originIds)

            val toUpsertMandaKeys = remoteNew.asEntity(todoIds)

            mandaKeyDao.upsertMandaKeys(toUpsertMandaKeys)

            Logger.d("originIds : $originIds")
            Logger.d("getMandaKeys : ${mandaKeyDao.getMandaKeys().first()}")
            Logger.d("todoIds : $todoIds")
            Logger.d("toUpsertMandaKeys : $toUpsertMandaKeys")
            Logger.d("getMandaKeys : ${mandaKeyDao.getMandaKeys().first()}")

            syncHelper.setMaxUpdateTime(
                toUpsertMandaKeys,
                updateTimeDataSource::setMandaKeyUpdateTime
            )
            return true
        } catch (e: Exception) {
            Logger.e("${e.message}")
            return false
        }
    }

    override suspend fun syncWrite(): Boolean {
        try {
            val localNew =
                mandaKeyDao.getToWriteMandaKeys(updateTimeDataSource.mandaKeyUpdateTime.first())

            val originIds = mandaKeyDataSource.upsertMandaKey(localNew.asNetworkModel())

            val toUpsertMandaKeys = localNew.asSyncedEntity(originIds)
            mandaKeyDao.upsertMandaKeys(toUpsertMandaKeys)

//            if (!userDataSource.isInit.first()) {
//                checkFinalManda()
//            }


            syncHelper.setMaxUpdateTime(
                toUpsertMandaKeys,
                updateTimeDataSource::setMandaKeyUpdateTime
            )
            return true
        } catch (e: Exception) {
            Logger.e("${e.message}")
            return false
        }
    }

//    suspend fun checkFinalManda() {
//        val finalManda = mandaKeyDao.getFinalManda().map { it != null }
//        if (finalManda.first()) {
//            userDataSource.updateInit(true)
//        }
//    }

}