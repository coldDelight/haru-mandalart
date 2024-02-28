package com.coldblue.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coldblue.domain.todo.GetTodoUseCase
import com.coldblue.domain.todo.ToggleTodoUseCase
import com.coldblue.domain.todo.UpsertTodoUseCase
import com.coldblue.domain.todogroup.DeleteCurrentGroupUseCase
import com.coldblue.domain.todogroup.GetCurrentGroupUseCase
import com.coldblue.domain.todogroup.GetGroupWithCurrentUseCase
import com.coldblue.domain.todogroup.GetTodoGroupUseCase
import com.coldblue.domain.todogroup.UpsertCurrentGroupUseCase
import com.coldblue.domain.todogroup.UpsertTodoGroupUseCase
import com.coldblue.model.CurrentGroup
import com.coldblue.model.Todo
import com.coldblue.model.TodoGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    getGroupWithCurrentUseCase: GetGroupWithCurrentUseCase,
    getTodoUseCase: GetTodoUseCase,
//    getTodoGroupUseCase: GetTodoGroupUseCase,
    private val upsertTodoGroupUseCase: UpsertTodoGroupUseCase,
    private val upsertCurrentGroupUseCase: UpsertCurrentGroupUseCase,
    private val upsertTodoUseCase: UpsertTodoUseCase,
    private val toggleTodoUseCase: ToggleTodoUseCase,
    private val deleteCurrentGroupUseCase: DeleteCurrentGroupUseCase
) : ViewModel() {
    private val _bottomSheetUiSate = MutableStateFlow<BottomSheetUiState>(BottomSheetUiState.Down)
    val bottomSheetUiSate: StateFlow<BottomSheetUiState> = _bottomSheetUiSate

    init {
        viewModelScope.launch {
//            upsertTodoGroup(TodoGroup("안드로이드"))
//            upsertTodoGroup(TodoGroup("블로그"))
//            upsertTodoGroup(TodoGroup("취업"))
//            upsertTodoGroup(TodoGroup("운동"))
//            upsertTodoUseCase(Todo("1번이요","내용입니다"))
//            upsertTodoUseCase(Todo("2번이요","내용입니다"))
//            upsertTodoUseCase(Todo("3번이요", "내용입니다", todoGroupId = 1))
//            upsertTodoUseCase(Todo("4번이요","내용입니다", todoGroupId = 2))
//            upsertTodoUseCase(Todo("4번이요","내용입니다", todoGroupId = 3))
//
//            upsertCurrentGroup(CurrentGroup(1, id = 1))
//            upsertCurrentGroup(CurrentGroup(2, id = 6))
        }
    }

    fun showSheet(content: ContentState) {
        viewModelScope.launch {
            _bottomSheetUiSate.value = BottomSheetUiState.Up(content)
        }
    }

    fun hideSheet() {
        viewModelScope.launch {
            _bottomSheetUiSate.value = BottomSheetUiState.Down
        }
    }


    val todoUiState: StateFlow<TodoUiState> =
        getGroupWithCurrentUseCase().combine(getTodoUseCase(LocalDate.now())) { group, todoList ->
            val todoGroupList = group.todoGroupList
            val currentGroupList = group.currentGroupList.groupBy { it.id }
            TodoUiState.Success(
                today = LocalDate.now(),
                todoList = todoList,
                todoGroupList = todoGroupList,
                currentGroupList = List(9) { index ->
                    val id = index + 1
                    if (currentGroupList.keys.contains(id)) {
                        val currentGroup = currentGroupList[id]!!.first()
                        val name = todoGroupList.first { it.id == currentGroup.todoGroupId }.name
                        val currentTodos =
                            todoList.filter { it.todoGroupId == currentGroup.todoGroupId }
                        val isDoing = currentTodos.all { it.isDone }
                        val hasTodo = currentTodos.isNotEmpty()
                        if (isDoing && hasTodo) {
                            CurrentGroupState.Done(
                                currentGroup = currentGroup,
                                name = name
                            )
                        } else {
                            CurrentGroupState.Doing(
                                name = name,
                                currentGroup = currentGroup,
                                leftTodo = currentTodos.filter { !it.isDone }.size.toString(),
                            )

                        }
                    } else if (id == 5) {
                        CurrentGroupState.Center(
                            totTodo = todoList.size.toString(),
                            doneTodo = todoList.filter { it.isDone }.size.toString(),
                        )
                    } else {
                        CurrentGroupState.Empty(
                            currentGroup = CurrentGroup(todoGroupId = -1, id = id)
                        )
                    }
                }.sortedBy { it.currentGroup.id }
            )
        }.catch {
            TodoUiState.Error(it.message ?: "Error")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodoUiState.Loading
        )

    fun upsertTodo(todo: Todo) {
        viewModelScope.launch {
            upsertTodoUseCase(todo)
        }
    }

    fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            toggleTodoUseCase(todo)
        }
    }

    fun upsertTodoGroup(todoGroup: TodoGroup) {
        viewModelScope.launch {
            upsertTodoGroupUseCase(todoGroup)
        }
    }

    fun upsertCurrentGroup(currentGroup: CurrentGroup) {
        viewModelScope.launch {
            upsertCurrentGroupUseCase(currentGroup)
        }
    }

    fun deleteCurrentGroup(currentGroupId: Int, todoGroupId: Int) {
        viewModelScope.launch {
            deleteCurrentGroupUseCase(currentGroupId, todoGroupId)
        }
    }
}