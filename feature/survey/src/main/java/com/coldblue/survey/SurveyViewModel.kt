package com.coldblue.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coldblue.data.util.LoginState
import com.coldblue.domain.auth.GetAuthStateUseCase
import com.coldblue.domain.network.GetNetworkStateUseCase
import com.coldblue.domain.survey.GetSurveyListUseCase
import com.coldblue.domain.survey.LikeSurveyUseCase
import com.coldblue.model.Survey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val getSurveyListUseCase: GetSurveyListUseCase,
    private val getNetworkStateUseCase: GetNetworkStateUseCase,
    getAuthStateUseCase: GetAuthStateUseCase,
    private val likeSurveyUseCase: LikeSurveyUseCase,
) : ViewModel() {
    private val _surveyUIState = MutableStateFlow<SurveyUiState>(SurveyUiState.Loading)
    val surveyUIState: StateFlow<SurveyUiState> get() = _surveyUIState

    val networkState = getNetworkStateUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val authState = getAuthStateUseCase().catch {
        LoginState.Loading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LoginState.Loading
    )


    fun getSurveyList() {
        viewModelScope.launch {
            if (getNetworkStateUseCase().first()) {
                val surveyList = getSurveyListUseCase()
                if (surveyList.isNotEmpty()) {
                    _surveyUIState.value = SurveyUiState.Success(surveyList)
                } else {
                    _surveyUIState.value = SurveyUiState.Error("제안하기 게시판이 비어있습니다.")
                }
            } else {
                _surveyUIState.value = SurveyUiState.Error("네트워크 연결 상태가 좋지 않습니다.")
            }
        }
    }

    fun updateSurvey(survey: Survey) {
        viewModelScope.launch {
            likeSurveyUseCase(
                survey.copy(
                    isLiked = !survey.isLiked,
                    likeCount = if (survey.isLiked) survey.likeCount - 1 else survey.likeCount + 1
                )
            )
            getSurveyList()
        }
    }

    init {
        getSurveyList()
    }
}