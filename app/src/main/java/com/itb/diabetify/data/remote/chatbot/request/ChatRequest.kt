package com.itb.diabetify.data.remote.chatbot.request

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("message")
    val message: String,
)
