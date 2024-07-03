package com.coldblue.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coldblue.domain.todo.GetMandaTodoGraphUseCase
import com.coldblue.domain.todo.GetTodoExistDateByIndexYearUseCase
import com.coldblue.domain.todo.GetMandaTodoByIndexDateUseCase
import com.coldblue.domain.todo.GetUniqueTodoYearUseCase
import com.coldblue.domain.todo.UpsertMandaTodoUseCase
import com.coldblue.model.MandaTodo
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getTodoExistDateByIndexYearUseCase: GetTodoExistDateByIndexYearUseCase,
    private val getMandaTodoByIndexDateUseCase: GetMandaTodoByIndexDateUseCase,
    private val getMandaTodoGraphUseCase: GetMandaTodoGraphUseCase,
    private val getUniqueTodoYearUseCase: GetUniqueTodoYearUseCase,
    private val upsertMandaTodoUseCase: UpsertMandaTodoUseCase
) : ViewModel() {
    init {
        // 그래프가 비었다면 index = -1
        viewModelScope.launch {
            val firstIndex = HistoryUtil.initCurrentIndex(getMandaTodoGraphUseCase().first())
            _currentIndex.value = firstIndex
        }
    }

    private val _currentYear = MutableStateFlow(LocalDate.now().year.toString())
    val currentYear: StateFlow<String> get() = _currentYear

    private val _currentDate = MutableStateFlow(LocalDate.now().toString())
    val currentDate: StateFlow<String> get() = _currentDate

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> get() = _currentIndex

    // graph 데이터를 가져오기
    // 존재하는 데이터 중 가장 왼쪽 데이터 선택
    val historyUIState: StateFlow<HistoryUIState> =
        currentIndex.flatMapLatest { index ->
            currentYear.flatMapLatest { year ->
                currentDate.flatMapLatest { date ->
                    combine(
                        getMandaTodoGraphUseCase(),
                        getMandaTodoByIndexDateUseCase(index, date),
                        getTodoExistDateByIndexYearUseCase(index, year),
                        getUniqueTodoYearUseCase()
                    ) { graphList, todoList, doneDateList, yearList ->
                        Logger.d(graphList)
                        Logger.d(todoList)
                        Logger.d(doneDateList)
                        Logger.d(yearList)
                        val titleBar = if(graphList.isNotEmpty()) {
                            TitleBar(
                                name = graphList[index].name,
                                startDate = if(doneDateList.isNotEmpty()) HistoryUtil.dateToString(doneDateList.first().toString()) else "",
                                rank = HistoryUtil.calculateRank(graphList, index),
                                colorIndex = graphList[index].colorIndex
                            )
                        } else {
                            TitleBar()
                        }

                        val historyController = if(doneDateList.isNotEmpty()){
                            HistoryController(
                                colorIndex = graphList[index].colorIndex,
                                allCount = graphList[index].allCount,
                                doneCount = graphList[index].doneCount,
                                donePercentage = if(graphList[index].allCount != 0) (graphList[index].doneCount / graphList[index].allCount * 100) else 0,
                                continueDate = if(doneDateList.isNotEmpty()) HistoryUtil.calculateContinueDate(doneDateList) else 0,
                                controller = HistoryUtil.makeController(
                                    year.toInt(),
                                    doneDateList
                                ),
                                years = yearList
                            )
                        }else{
                            HistoryController()
                        }

                        val todoController = if(todoList.isNotEmpty()){
                            TodoController(
                                date = date,
                                dayAllCount = todoList.size,
                                dayDoneCount = todoList.filter { it.isDone }.size,
                                todoList = todoList
                            )
                        }else{
                            TodoController()
                        }

                        Logger.d(titleBar)
                        Logger.d(historyController)

                        HistoryUIState.Success(
                            todoGraph = graphList.ifEmpty { emptyList() },
                            titleBar = titleBar,
                            historyController = historyController,
                            todoController = todoController
                        )
                    }.catch {
                        Logger.d(it)
                    }
                }
            }
        }.catch {
            HistoryUIState.Error(it.message ?: "Error")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUIState.Loading
        )

    fun changeYear(year: String) {
        _currentYear.value = year
    }

    fun changeDay(day: String) {
        _currentDate.value = day
    }

    fun changeCurrentIndex(index: Int) {
        _currentIndex.value = index
    }

    fun updateMandaTodo(todo: MandaTodo) {
        Logger.d(todo)
        viewModelScope.launch {
            upsertMandaTodoUseCase(todo)
        }
    }

    fun deleteMandaTodo(todo: MandaTodo) {
        viewModelScope.launch {
            upsertMandaTodoUseCase(todo.copy(isDel = true))
        }
    }
}