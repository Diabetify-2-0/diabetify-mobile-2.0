package com.itb.diabetify.domain.usecases.chatbot

import com.itb.diabetify.domain.model.ChatStreamEvent
import com.itb.diabetify.domain.repository.ChatbotRepository
import kotlinx.coroutines.flow.Flow

class SendChatMessageStreamUseCase(
    private val repository: ChatbotRepository,
) {
    operator fun invoke(userId: String, message: String): Flow<ChatStreamEvent> {
        return repository.sendMessageStream(userId, message)
    }
}
