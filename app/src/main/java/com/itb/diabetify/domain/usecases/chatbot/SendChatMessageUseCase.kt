package com.itb.diabetify.domain.usecases.chatbot

import com.itb.diabetify.domain.repository.ChatbotRepository
import com.itb.diabetify.util.Resource

class SendChatMessageUseCase(
    private val repository: ChatbotRepository,
) {
    suspend operator fun invoke(userId: String, message: String): Resource<String> {
        return repository.sendMessage(userId, message)
    }
}
