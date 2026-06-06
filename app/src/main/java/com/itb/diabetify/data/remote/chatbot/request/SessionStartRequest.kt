package com.itb.diabetify.data.remote.chatbot.request

import com.google.gson.annotations.SerializedName

data class SessionStartRequest(
    @SerializedName("user_id")
    val userId: String,
)
