package com.itb.diabetify.data.remote.chatbot.response

import com.google.gson.annotations.SerializedName

data class RecommendationResponse(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("question_1")
    val question1: String,
    @SerializedName("question_2")
    val question2: String,
    @SerializedName("is_dirty")
    val isDirty: Boolean,
)
