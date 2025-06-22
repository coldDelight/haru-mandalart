package com.coldblue.network.datasourceImpl

import com.coldblue.network.datasource.SurveyDataSource
import com.coldblue.network.model.NetworkNotice
import com.coldblue.network.model.NetworkSurvey
import com.coldblue.network.model.NetworkSurveyComment
import com.coldblue.network.model.NetworkSurveyLike
import com.coldblue.network.model.NetworkSurveyLikeWithIsLike
import com.orhanobut.logger.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject

class SurveyDataSourceImpl @Inject constructor(
    private val client: SupabaseClient
) : SurveyDataSource {
    override suspend fun getSurveyList(): List<NetworkSurvey> {
        return try {
            client.postgrest["survey"].select().decodeList<NetworkSurvey>()
        } catch (e: Exception) {
            Logger.d(e.message)
            emptyList()
        }
    }

    override suspend fun getSurveyLikedList(): List<NetworkSurveyLikeWithIsLike> {
        return try {
            val userId = client.auth.currentUserOrNull()?.id ?:"noId"
            val surveyLikeList = client.postgrest["surveyLike"].select().decodeList<NetworkSurveyLike>()

            surveyLikeList.map { surveyLike -> NetworkSurveyLikeWithIsLike(isLiked = surveyLike.user_id==userId, id = surveyLike.id, survey_id = surveyLike.survey_id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getSurvey(id: Int): NetworkSurvey {
        return try {
            client.postgrest["survey"].select {
                filter {
                    NetworkNotice::id eq id
                }
            }.decodeSingle<NetworkSurvey>()
        } catch (e: Exception) {
            NetworkSurvey(0, "", "", "", "", true, 0)
        }
    }

    override suspend fun upsertSurvey(survey: NetworkSurvey) {
        try {
            client.postgrest["survey"].insert(survey)
        } catch (e: Exception) {
        }
    }

    override suspend fun isSurveyLiked(id: Int): Boolean {
        return try {
            client.postgrest["surveyLike"].select {
                filter {
                    NetworkSurveyLike::survey_id eq id
                }
            }.decodeList<NetworkSurveyLike>().isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSurveyLiked(id: Int): List<NetworkSurveyLikeWithIsLike> {
        return try {
            val userId = client.auth.currentUserOrNull()?.id ?:"noId"

            val surveyLike =
            client.postgrest["surveyLike"].select {
                filter {
                    NetworkSurveyLike::survey_id eq id
                }
            }.decodeList<NetworkSurveyLike>()

            surveyLike.map { NetworkSurveyLikeWithIsLike(isLiked = it.user_id==userId, id = it.id, survey_id = it.survey_id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAllSurveyCommentList(): List<NetworkSurveyComment> {
        return try {
            client.postgrest["surveyComment"].select().decodeList<NetworkSurveyComment>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getSurveyCommentList(surveyId: Int): List<NetworkSurveyComment> {
        return try {
            client.postgrest["surveyComment"].select {
                filter {
                    NetworkSurveyComment::survey_id eq surveyId
                }
            }.decodeList<NetworkSurveyComment>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun upsertSurveyComment(surveyComment: NetworkSurveyComment) {
        try {
            client.postgrest["surveyComment"].insert(surveyComment)
        } catch (e: Exception) {
        }
    }

    override suspend fun likeSurvey(id: Int, likeCount: Int) {
        try {
            client.postgrest["survey"].update(
                {
                    NetworkSurvey::like_count setTo likeCount
                }
            ) {
                filter {
                    NetworkSurvey::id eq id
                }
            }

            val like = NetworkSurveyLike(survey_id = id)
            client.postgrest["surveyLike"].insert(like)

        } catch (e: Exception) {
        }
    }

    override suspend fun likeCancelSurvey(id: Int, likeCount: Int) {
        try {
            client.postgrest["survey"].update(
                {
                    NetworkSurvey::like_count setTo likeCount
                }
            ) {
                filter {
                    NetworkSurvey::id eq id
                }
            }
            client.postgrest["surveyLike"].delete {
                filter {
                    NetworkSurveyLike::survey_id eq id
                }
            }
        } catch (e: Exception) {
        }
    }
}