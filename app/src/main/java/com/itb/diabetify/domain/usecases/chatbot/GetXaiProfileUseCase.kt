package com.itb.diabetify.domain.usecases.chatbot

import com.itb.diabetify.domain.model.XaiProfile
import com.itb.diabetify.domain.repository.ChatbotRepository
import com.itb.diabetify.util.Resource

class GetXaiProfileUseCase(
    private val repository: ChatbotRepository,
) {
    suspend operator fun invoke(userId: String): Resource<XaiProfile> {
        return repository.getXaiProfile(userId)
    }
}
