package com.itb.diabetify.domain.usecases.chatbot

import com.itb.diabetify.domain.model.ChatMessage
import com.itb.diabetify.domain.repository.ChatbotRepository
import com.itb.diabetify.util.Resource

class LoadChatHistoryUseCase(
    private val repository: ChatbotRepository,
) {
    suspend operator fun invoke(userId: String): Resource<List<ChatMessage>> {
        return repository.loadHistory(userId)
    }
}
