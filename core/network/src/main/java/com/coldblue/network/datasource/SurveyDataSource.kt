package com.coldblue.network.datasource

import com.coldblue.network.model.NetworkSurvey
import com.coldblue.network.model.NetworkSurveyComment
import com.coldblue.network.model.NetworkSurveyLike
import com.coldblue.network.model.NetworkSurveyLikeWithIsLike

interface SurveyDataSource {
    suspend fun getSurveyList(): List<NetworkSurvey>
    suspend fun getSurveyLikedList(): List<NetworkSurveyLikeWithIsLike>

    suspend fun getSurvey(id: Int): NetworkSurvey
    suspend fun upsertSurvey(survey: NetworkSurvey)
    suspend fun isSurveyLiked(id: Int): Boolean
    suspend fun getSurveyLiked(id: Int): List<NetworkSurveyLikeWithIsLike>

    suspend fun getAllSurveyCommentList(): List<NetworkSurveyComment>
    suspend fun getSurveyCommentList(surveyId: Int): List<NetworkSurveyComment>
    suspend fun upsertSurveyComment(surveyComment: NetworkSurveyComment)


    suspend fun likeSurvey(id: Int, likeCount: Int)
    suspend fun likeCancelSurvey(id: Int, likeCount: Int)



}