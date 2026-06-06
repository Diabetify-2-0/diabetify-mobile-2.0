package com.itb.diabetify.domain.model

sealed class ChatStreamEvent {
    data class Chunk(val delta: String) : ChatStreamEvent()
    data object Done : ChatStreamEvent()
    data class Error(val message: String) : ChatStreamEvent()
}
