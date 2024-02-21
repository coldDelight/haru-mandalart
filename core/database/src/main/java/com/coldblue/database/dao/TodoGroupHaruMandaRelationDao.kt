package com.coldblue.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coldblue.database.entity.MandaEntity
import com.coldblue.database.entity.TodoGroupHaruMandaRelationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoGroupHaruMandaRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTodoGroupHaruMandaEntities(relationEntities: List<TodoGroupHaruMandaRelationEntity>)
    @Query("Select * From todo_group_haru_manda_relation")
    fun getMandaTodoGroupHaruMandaEntities():  Flow<List<TodoGroupHaruMandaRelationEntity>>
    @Query("Delete From todo_group_haru_manda_relation")
    fun deleteTodoGroupHaruMandaEntities()
}