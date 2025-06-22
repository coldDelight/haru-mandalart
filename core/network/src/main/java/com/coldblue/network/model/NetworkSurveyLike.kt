package com.coldblue.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkSurveyLike(
    val id: Int = 0,
    val survey_id: Int,
    val user_id: String? = null,
)

data class NetworkSurveyLikeWithIsLike(
    val id: Int = 0,
    val survey_id: Int,
    val isLiked :Boolean
)
