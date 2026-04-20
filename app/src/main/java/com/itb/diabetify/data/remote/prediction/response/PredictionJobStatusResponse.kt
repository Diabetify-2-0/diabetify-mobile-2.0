package com.itb.diabetify.data.remote.prediction.response

import com.google.gson.annotations.SerializedName

data class PredictionJobStatusResponse (
    @SerializedName("data")
    val data: PredictionJobStatusData,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class PredictionJobStatusData(
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("job_id")
    val jobId: String? = null,
    @SerializedName("progress")
    val progress: Int? = null,
    @SerializedName("result")
    val result: PredictionJobResult? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("step")
    val step: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("error")
    val error: String? = null
)

data class PredictionJobResult(
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("prediction_id")
    val predictionId: Int? = null,
    @SerializedName("risk_percentage")
    val riskPercentage: Double? = null,
    @SerializedName("risk_score")
    val riskScore: Double? = null
)
