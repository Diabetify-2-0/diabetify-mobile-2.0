package com.itb.diabetify.domain.usecases.chatbot

data class ChatbotUseCases(
    val sendChatMessage: SendChatMessageUseCase,
    val sendChatMessageStream: SendChatMessageStreamUseCase,
    val loadChatHistory: LoadChatHistoryUseCase,
    val loadRecommendations: LoadRecommendationsUseCase,
    val refreshRecommendations: RefreshRecommendationsUseCase,
    val getXaiProfile: GetXaiProfileUseCase,
)
