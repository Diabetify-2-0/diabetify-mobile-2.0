package com.itb.diabetify.domain.usecases.chatbot

import com.itb.diabetify.domain.model.ChatRecommendation
import com.itb.diabetify.domain.repository.ChatbotRepository
import com.itb.diabetify.util.Resource

class LoadRecommendationsUseCase(
    private val repository: ChatbotRepository,
) {
    suspend operator fun invoke(userId: String): Resource<ChatRecommendation> {
        return repository.loadRecommendations(userId)
    }
}
