package com.itb.diabetify.data.remote.prediction.response

import com.google.gson.annotations.SerializedName

data class GetPredictionResponse (
    @SerializedName("data")
    val data: List<PredictionData?> = emptyList(),
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class PredictionData(
    @SerializedName("risk_score")
    val riskScore: Double? = null,
    @SerializedName("prediction_summary")
    val predictionSummary: String? = null,
    @SerializedName("age")
    val age: Int? = null,
    @SerializedName("age_contribution")
    val ageContribution: Double? = null,
    @SerializedName("age_impact")
    val ageImpact: Int? = null,
    @SerializedName("age_explanation")
    val ageExplanation: String? = null,
    @SerializedName("bmi")
    val bmi: Double? = null,
    @SerializedName("bmi_contribution")
    val bmiContribution: Double? = null,
    @SerializedName("bmi_impact")
    val bmiImpact: Int? = null,
    @SerializedName("bmi_explanation")
    val bmiExplanation: String? = null,
    @SerializedName("brinkman_score")
    val brinkmanScore: Int? = null,
    @SerializedName("brinkman_score_contribution")
    val brinkmanScoreContribution: Double? = null,
    @SerializedName("brinkman_score_impact")
    val brinkmanScoreImpact: Int? = null,
    @SerializedName("brinkman_score_explanation")
    val brinkmanScoreExplanation: String? = null,
    @SerializedName("is_hypertension")
    val isHypertension: Boolean? = null,
    @SerializedName("is_hypertension_contribution")
    val isHypertensionContribution: Double? = null,
    @SerializedName("is_hypertension_impact")
    val isHypertensionImpact: Int? = null,
    @SerializedName("is_hypertension_explanation")
    val isHypertensionExplanation: String? = null,
    @SerializedName("is_cholesterol")
    val isCholesterol: Boolean? = null,
    @SerializedName("is_cholesterol_contribution")
    val isCholesterolContribution: Double? = null,
    @SerializedName("is_cholesterol_impact")
    val isCholesterolImpact: Int? = null,
    @SerializedName("is_cholesterol_explanation")
    val isCholesterolExplanation: String? = null,
    @SerializedName("is_bloodline")
    val isBloodline: Boolean? = null,
    @SerializedName("is_bloodline_contribution")
    val isBloodlineContribution: Double? = null,
    @SerializedName("is_bloodline_impact")
    val isBloodlineImpact: Int? = null,
    @SerializedName("is_bloodline_explanation")
    val isBloodlineExplanation: String? = null,
    @SerializedName("is_macrosomic_baby")
    val isMacrosomicBaby: Int? = null,
    @SerializedName("is_macrosomic_baby_contribution")
    val isMacrosomicBabyContribution: Double? = null,
    @SerializedName("is_macrosomic_baby_impact")
    val isMacrosomicBabyImpact: Int? = null,
    @SerializedName("is_macrosomic_baby_explanation")
    val isMacrosomicBabyExplanation: String? = null,
    @SerializedName("smoking_status")
    val smokingStatus: String? = null,
    @SerializedName("smoking_status_contribution")
    val smokingStatusContribution: Double? = null,
    @SerializedName("smoking_status_impact")
    val smokingStatusImpact: Int? = null,
    @SerializedName("smoking_status_explanation")
    val smokingStatusExplanation: String? = null,
    @SerializedName("avg_smoke_count")
    val avgSmokeCount: Int? = null,
    @SerializedName("physical_activity_frequency")
    val physicalActivityFrequency: Int? = null,
    @SerializedName("physical_activity_frequency_contribution")
    val physicalActivityFrequencyContribution: Double? = null,
    @SerializedName("physical_activity_frequency_impact")
    val physicalActivityFrequencyImpact: Int? = null,
    @SerializedName("physical_activity_frequency_explanation")
    val physicalActivityFrequencyExplanation: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)
