package com.coldblue.database.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.coldblue.database.entity.MandaTodoEntity
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.Flow

@Dao
interface MandaTodoDao {
    @Query("SELECT * FROM manda_todo WHERE is_del = 0")
    fun getMandaTodo(): Flow<List<MandaTodoEntity>>

    @Transaction
    fun getMandaTodoIdByOriginIds(originIds: List<Int>): List<Int?> {
        return originIds.map { originId ->
            getMandaTodoIdByOriginId(originId)
        }
    }

    @Query("SELECT id FROM manda_todo WHERE origin_id = :originId")
    fun getMandaTodoIdByOriginId(originId: Int): Int?

    @Query("SELECT * FROM manda_todo WHERE update_time > :updateTime AND is_sync=0")
    fun getToWriteMandaTodos(updateTime: String): List<MandaTodoEntity>

    @Transaction
    fun getAllMandaTodoCount(): List<Pair<Int, Int>>{

        TODO() 여기를 쳐내야할듯

        Logger.e("Start DAO")

        val resultList = mutableListOf<Pair<Int, Int>>()
        for(index in 0..8){
            val allCount = getMandaTodoIndexCount(index) ?: 0
            val doneCount = getMandaTodoIndexDoneCount(index) ?: 0
            resultList.add(Pair(allCount, doneCount))
            Logger.d(resultList)
        }
        return resultList.toList()
    }

    @Query("SELECT COUNT(id) FROM manda_todo WHERE manda_index = :index AND is_del = 0")
    fun getMandaTodoIndexCount(index: Int): Int?

    @Query("SELECT COUNT(id) FROM manda_todo WHERE manda_index = :index AND is_del = 0 AND is_done = 1")
    fun getMandaTodoIndexDoneCount(index: Int): Int?

    @Query("SELECT * FROM manda_todo WHERE manda_index = :index AND is_del = 0")
    fun getMandaTodoByIndex(index: Int): Flow<List<MandaTodoEntity>>

    @Query("Update manda_todo Set is_del = 1, is_Sync = 0, update_time = :date")
    suspend fun deleteAllMandaTodo(date: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMandaTodo(mandaTodo: MandaTodoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMandaTodo(mandaTodo: List<MandaTodoEntity>)

}