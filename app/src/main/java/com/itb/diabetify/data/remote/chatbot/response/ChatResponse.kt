package com.itb.diabetify.data.remote.chatbot.response

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("response")
    val response: String,
    @SerializedName("timestamp")
    val timestamp: String?,
)
