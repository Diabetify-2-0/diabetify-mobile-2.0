package com.itb.diabetify.data.remote.chatbot.response

import com.google.gson.annotations.SerializedName

data class ChatHistoryResponse(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("total")
    val total: Int,
    @SerializedName("entries")
    val entries: List<ChatHistoryEntry>,
)

data class ChatHistoryEntry(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("summary")
    val summary: String?,
    @SerializedName("timestamp")
    val timestamp: String?,
)
