package com.coldblue.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.coldblue.database.dao.CurrentGroupDao
import com.coldblue.database.dao.HaruMandaDao
import com.coldblue.database.dao.MandaDao
import com.coldblue.database.dao.MandaDetailDao
import com.coldblue.database.dao.TodoDao
import com.coldblue.database.dao.TodoGroupDao
import com.coldblue.database.dao.TodoGroupHaruMandaRelationDao
import com.coldblue.database.entity.CurrentGroupEntity
import com.coldblue.database.entity.HaruMandaEntity
import com.coldblue.database.entity.MandaDetailEntity
import com.coldblue.database.entity.MandaEntity
import com.coldblue.database.entity.TodoEntity
import com.coldblue.database.entity.TodoGroupEntity
import com.coldblue.database.entity.TodoGroupHaruMandaRelationEntity

@Database(
    entities = [
        CurrentGroupEntity::class,
        HaruMandaEntity::class,
        MandaEntity::class,
        MandaDetailEntity::class,
        TodoEntity::class,
        TodoGroupEntity::class,
        TodoGroupHaruMandaRelationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDataBase: RoomDatabase() {
    abstract fun currentGroupDao(): CurrentGroupDao
    abstract fun haruMandaDao(): HaruMandaDao
    abstract fun mandaDao(): MandaDao
    abstract fun mandaDetailDao(): MandaDetailDao
    abstract fun todoDao(): TodoDao
    abstract fun todoGroupDao(): TodoGroupDao
    abstract fun todoGroupHaruMandaRelationDao(): TodoGroupHaruMandaRelationDao

}