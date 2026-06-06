package com.itb.diabetify.data.remote.chatbot.response

import com.google.gson.annotations.SerializedName

data class XaiFeatureResponse(
    @SerializedName("feature") val feature: String,
    @SerializedName("alias") val alias: String,
    @SerializedName("value") val value: Double,
    @SerializedName("shap") val shap: Double,
    @SerializedName("contribution") val contribution: Double,
    @SerializedName("impact") val impact: String,
    @SerializedName("explanation") val explanation: String,
)

data class XaiProfileResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("risk_score") val riskScore: Double,
    @SerializedName("features") val features: List<XaiFeatureResponse>,
    @SerializedName("xai_summary") val xaiSummary: String?,
    @SerializedName("updated_at") val updatedAt: String,
)
