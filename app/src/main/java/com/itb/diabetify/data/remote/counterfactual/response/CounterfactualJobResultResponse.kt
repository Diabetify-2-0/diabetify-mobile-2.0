package com.itb.diabetify.data.remote.counterfactual.response

import com.google.gson.annotations.SerializedName

data class CounterfactualJobResultResponse(
    @SerializedName("data")
    val data: CounterfactualJobResultData? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class CounterfactualJobResultData(
    @SerializedName("job_id")
    val jobId: String? = null,
    @SerializedName("job_status")
    val jobStatus: String? = null,
    @SerializedName("reason_code")
    val reasonCode: String? = null,
    @SerializedName("result")
    val result: CounterfactualResultPayload? = null
)

data class CounterfactualResultPayload(
    @SerializedName("candidate")
    val candidate: CounterfactualCandidate? = null,
    @SerializedName("input")
    val input: CounterfactualInput? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("reason_code")
    val reasonCode: String? = null,
    @SerializedName("runtime_ms")
    val runtimeMs: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("validation")
    val validation: CounterfactualValidationSummary? = null
)

data class CounterfactualInput(
    @SerializedName("class")
    val className: String? = null,
    @SerializedName("probability_low_risk")
    val probabilityLowRisk: Double? = null,
    @SerializedName("mutable_allowed")
    val mutableAllowed: List<String> = emptyList(),
    @SerializedName("immutable_features")
    val immutableFeatures: List<String> = emptyList()
)

data class CounterfactualCandidate(
    @SerializedName("candidate_id")
    val candidateId: String,
    @SerializedName("features")
    val features: Map<String, Any?> = emptyMap(),
    @SerializedName("candidate_prediction")
    val candidatePrediction: CounterfactualPredictionInfo? = null,
    @SerializedName("lof_score")
    val lofScore: Double? = null,
    @SerializedName("changed_features")
    val changedFeatures: List<CounterfactualChangedFeature> = emptyList(),
    @SerializedName("validation")
    val validation: CounterfactualValidationSummary? = null
)

data class CounterfactualPredictionInfo(
    @SerializedName("class")
    val className: String? = null,
    @SerializedName("probability_low_risk")
    val probabilityLowRisk: Double? = null
)

data class CounterfactualValidationSummary(
    @SerializedName("immutable_violation")
    val immutableViolation: Boolean? = null,
    @SerializedName("mutable_violation")
    val mutableViolation: Boolean? = null,
    @SerializedName("medical_rules_passed")
    val medicalRulesPassed: Boolean? = null
)

data class CounterfactualChangedFeature(
    @SerializedName("baseline_value")
    val baselineValue: Double? = null,
    @SerializedName("candidate_value")
    val candidateValue: Double? = null,
    @SerializedName("delta")
    val delta: Double? = null,
    @SerializedName("direction")
    val direction: String? = null,
    @SerializedName("feature_name")
    val featureName: String
)
