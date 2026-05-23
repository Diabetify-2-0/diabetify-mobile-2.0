package com.itb.diabetify.data.remote.prediction.response

import com.google.gson.annotations.SerializedName

data class GetPredictionScoreResponse (
    @SerializedName("data")
    val data: List<PredictionScoreData?> = emptyList(),
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class PredictionScoreData(
    @SerializedName("risk_score")
    val riskScore: Double? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)
