package com.coldblue.survey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coldblue.data.util.LoginState
import com.coldblue.domain.auth.GetAuthStateUseCase
import com.coldblue.domain.network.GetNetworkStateUseCase
import com.coldblue.domain.survey.GetSurveyCommentUseCase
import com.coldblue.domain.survey.GetSurveyUseCase
import com.coldblue.domain.survey.LikeSurveyUseCase
import com.coldblue.domain.survey.UpsertSurveyCommentUseCase
import com.coldblue.model.Survey
import com.coldblue.model.SurveyComment
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
class SurveyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getAuthStateUseCase: GetAuthStateUseCase,
    private val getSurveyUseCase: GetSurveyUseCase,
    private val getSurveyCommentUseCase: GetSurveyCommentUseCase,
    private val upsertSurveyCommentUseCase: UpsertSurveyCommentUseCase,
    private val likeSurveyUseCase: LikeSurveyUseCase,
    private val getNetworkStateUseCase: GetNetworkStateUseCase,

    ) : ViewModel() {

    private val id: Int? = savedStateHandle.get<Int>("id")
    private val _survey = MutableStateFlow<Survey?>(null)
    private val _surveyComment = MutableStateFlow<List<SurveyComment>>(emptyList())

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

    val surveyState: StateFlow<Survey?> get() = _survey
    val surveyCommentState: StateFlow<List<SurveyComment>> get() = _surveyComment

    init {
        getSurvey()
        getSurveyComment()
    }

    private fun getSurvey() {
        viewModelScope.launch {
            if (getNetworkStateUseCase().first()) {
                if (id != null) {
                    _survey.value = getSurveyUseCase(id)
                }
            }
        }
    }

    fun updateSurvey(survey: Survey) {
        viewModelScope.launch {
            _survey.value = survey.copy(
                isLiked = !survey.isLiked,
                likeCount = if (survey.isLiked) survey.likeCount - 1 else survey.likeCount + 1
            )
            likeSurveyUseCase(surveyState.value!!)
        }
    }

    private fun getSurveyComment() {
        viewModelScope.launch {
            if (getNetworkStateUseCase().first()) {
                if (id != null) {
                    _surveyComment.value = getSurveyCommentUseCase(id)
                }
            }
        }
    }

    fun upsertSurveyComment(surveyComment: SurveyComment) {
        viewModelScope.launch {
            upsertSurveyCommentUseCase(surveyComment)
            _surveyComment.value = getSurveyCommentUseCase(surveyComment.surveyId)

        }
    }

}