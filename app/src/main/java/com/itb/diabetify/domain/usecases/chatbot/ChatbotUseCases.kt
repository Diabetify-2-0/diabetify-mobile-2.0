package com.itb.diabetify.domain.usecases.chatbot

data class ChatbotUseCases(
    val sendChatMessage: SendChatMessageUseCase,
    val loadChatHistory: LoadChatHistoryUseCase,
    val loadRecommendations: LoadRecommendationsUseCase,
    val refreshRecommendations: RefreshRecommendationsUseCase,
)
