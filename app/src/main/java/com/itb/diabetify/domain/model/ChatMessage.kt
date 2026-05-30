package com.itb.diabetify.domain.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
)
