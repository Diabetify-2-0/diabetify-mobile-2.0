package com.itb.diabetify.domain.repository

import com.itb.diabetify.domain.model.ChatMessage
import com.itb.diabetify.domain.model.ChatRecommendation
import com.itb.diabetify.util.Resource

interface ChatbotRepository {
    suspend fun sendMessage(userId: String, message: String): Resource<String>
    suspend fun loadHistory(userId: String): Resource<List<ChatMessage>>
    suspend fun startRecommendationSession(userId: String): Resource<ChatRecommendation>
    suspend fun getRecommendations(userId: String): Resource<ChatRecommendation>
    suspend fun loadRecommendations(userId: String): Resource<ChatRecommendation>
}
