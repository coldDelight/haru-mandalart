package com.coldblue.data.repository.todo

import com.coldblue.data.alarm.AlarmScheduler
import com.coldblue.data.mapper.asDomain
import com.coldblue.data.mapper.asEntity
import com.coldblue.data.mapper.asNetworkModel
import com.coldblue.data.mapper.asSyncedEntity
import com.coldblue.data.sync.SyncHelper
import com.coldblue.data.util.isNotToday
import com.coldblue.data.util.toFirstLocalDate
import com.coldblue.data.util.toLastLocalDate
import com.coldblue.data.util.toSoredIntList
import com.coldblue.database.dao.TodoDao
import com.coldblue.datastore.UpdateTimeDataSource
import com.coldblue.model.AlarmItem
import com.coldblue.model.Todo
import com.coldblue.network.datasource.TodoDataSource
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao,
    private val alarmScheduler: AlarmScheduler,
    private val todoDataSource: TodoDataSource,
    private val syncHelper: SyncHelper,
    private val updateTimeDataSource: UpdateTimeDataSource,
) : TodoRepository {

    override suspend fun upsertTodo(todo: Todo) {
        todoDao.upsertTodo(todo.asEntity())
        todo.syncAlarm()
        syncHelper.syncWrite()
    }

    override fun getTodo(date: LocalDate): Flow<List<Todo>> {
        return todoDao.getTodo(date).map { it.asDomain() }
    }

    override fun getTodo(todoId: Int): Flow<Todo> {
        return todoDao.getTodo(todoId).map {
            if (todoId == -1) Todo("") else it.asDomain()
        }
    }

    override fun getYearlyExistTodoDate(year: Int): Flow<List<LocalDate>> {
        return todoDao.getYearlyExistTodoDate(year.toFirstLocalDate(), year.toLastLocalDate())
    }

    override fun getTodoYearList(): Flow<List<Int>> {
        return todoDao.getUniqueTodoYear().map { it.toSoredIntList() }
//        return todoDao.getUniqueTodoYear().map { it?.map { it.toInt() }?.sorted() ?: emptyList() }
//        return todoDao.getTodoMinYear().combine(todoDao.getTodoMaxYear()) { minYear, maxYear ->
//            if (minYear != null && maxYear != null)
//                (minYear.year..maxYear.year).toList()
//            else
//                listOf(LocalDate.now().year)
//        }
    }

    override fun getUniqueTodoCountByDate(): Flow<Int> {
        return todoDao.getUniqueTodoCountByDate()
    }

    override suspend fun delTodo(todoId: Int) {
        todoDao.deleteTodo(todoId)
    }

    override suspend fun syncRead(): Boolean {
        try {
            val remoteNew = todoDataSource.getTodo(updateTimeDataSource.todoUpdateTime.first())
            val originIds = remoteNew.map { it.id }
            val todoIds = todoDao.getTodoIdByOriginIds(originIds)
            val toUpsertTodos = remoteNew.asEntity(todoIds)
            todoDao.upsertTodo(toUpsertTodos)
            syncHelper.setMaxUpdateTime(toUpsertTodos, updateTimeDataSource::setTodoUpdateTime)
            return true
        } catch (e: Exception) {
            Logger.e("${e.message}")
            return false
        }
    }

    override suspend fun syncWrite(): Boolean {
        try {
            val localNew = todoDao.getToWriteTodos(updateTimeDataSource.todoUpdateTime.first())

            val originIds = todoDataSource.upsertTodo(localNew.asNetworkModel())
            val toUpsertTodos = localNew.asSyncedEntity(originIds)
            todoDao.upsertTodo(toUpsertTodos)
            syncHelper.setMaxUpdateTime(toUpsertTodos, updateTimeDataSource::setTodoUpdateTime)
            return true
        } catch (e: Exception) {
            Logger.e("${e.message}")
            return false
        }
    }


    private fun Todo.syncAlarm() {
        if (time == null) {
            alarmScheduler.cancel(AlarmItem(id = id))
            return
        }
        if (date.isNotToday()) return
        if (isDel or isDone) {
            alarmScheduler.cancel(AlarmItem(LocalDateTime.of(date, time), title, id))
            return
        }
        alarmScheduler.schedule(AlarmItem(LocalDateTime.of(date, time), title, id))
    }

}