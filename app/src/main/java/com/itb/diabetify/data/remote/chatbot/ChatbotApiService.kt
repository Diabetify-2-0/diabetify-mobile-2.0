package com.itb.diabetify.data.remote.chatbot

import com.itb.diabetify.data.remote.chatbot.request.ChatRequest
import com.itb.diabetify.data.remote.chatbot.request.SessionStartRequest
import com.itb.diabetify.data.remote.chatbot.response.ChatHistoryResponse
import com.itb.diabetify.data.remote.chatbot.response.ChatResponse
import com.itb.diabetify.data.remote.chatbot.response.RecommendationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatbotApiService {
    @POST("api/v1/chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @GET("api/v1/history/{userId}")
    suspend fun getHistory(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 20,
    ): ChatHistoryResponse

    @POST("api/v1/recommendations/session/start")
    suspend fun startRecommendationSession(
        @Body request: SessionStartRequest,
    ): RecommendationResponse

    @GET("api/v1/recommendations/{userId}")
    suspend fun getRecommendations(
        @Path("userId") userId: String,
    ): RecommendationResponse
}
