package com.itb.diabetify.domain.model

data class XaiFeature(
    val feature: String,
    val alias: String,
    val value: Double,
    val shap: Double,
    val contribution: Double,
    val impact: String,
    val explanation: String,
)

data class XaiProfile(
    val userId: String,
    val riskScore: Double,
    val features: List<XaiFeature>,
    val xaiSummary: String?,
)
