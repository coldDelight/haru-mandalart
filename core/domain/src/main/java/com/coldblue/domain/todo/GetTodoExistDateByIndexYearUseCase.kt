package com.coldblue.domain.todo

import com.coldblue.data.repository.todo.MandaTodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodoExistDateByIndexYearUseCase @Inject constructor(
    private val mandaTodoRepository: MandaTodoRepository
) {
    operator fun invoke(index: Int, year: String): Flow<List<String>> =
        mandaTodoRepository.getTodoExistDateByIndexYear(index, year)
}