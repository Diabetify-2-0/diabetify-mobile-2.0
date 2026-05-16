package com.itb.diabetify.data.remote.counterfactual.response

import com.google.gson.annotations.SerializedName

data class CounterfactualJobResultResponse(
    @SerializedName("data")
    val data: CounterfactualJobResultData,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)

data class CounterfactualJobResultData(
    @SerializedName("job_id")
    val jobId: String,
    @SerializedName("job_status")
    val jobStatus: String,
    @SerializedName("reason_code")
    val reasonCode: String? = null,
    @SerializedName("result")
    val result: CounterfactualResultPayload? = null
)

data class CounterfactualResultPayload(
    @SerializedName("candidates")
    val candidates: List<CounterfactualCandidate> = emptyList(),
    @SerializedName("input_prediction")
    val inputPrediction: CounterfactualPredictionInfo? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("planner_input")
    val plannerInput: CounterfactualPlannerInput? = null,
    @SerializedName("prescriptive_plan")
    val prescriptivePlan: CounterfactualPrescriptivePlan? = null,
    @SerializedName("reason_code")
    val reasonCode: String? = null,
    @SerializedName("runtime_ms")
    val runtimeMs: Int? = null,
    @SerializedName("status")
    val status: String? = null
)

data class CounterfactualCandidate(
    @SerializedName("candidate_id")
    val candidateId: String,
    @SerializedName("metrics")
    val metrics: CounterfactualCandidateMetrics? = null,
    @SerializedName("prediction")
    val prediction: CounterfactualPredictionInfo? = null
)

data class CounterfactualCandidateMetrics(
    @SerializedName("changed_feature_count")
    val changedFeatureCount: Int? = null,
    @SerializedName("constraint_violations")
    val constraintViolations: Int? = null,
    @SerializedName("distance_l1")
    val distanceL1: Double? = null,
    @SerializedName("lof_score")
    val lofScore: Double? = null
)

data class CounterfactualPredictionInfo(
    @SerializedName("class")
    val className: String? = null,
    @SerializedName("probability_low_risk")
    val probabilityLowRisk: Double? = null
)

data class CounterfactualPlannerInput(
    @SerializedName("candidate_metrics")
    val candidateMetrics: CounterfactualCandidateMetrics? = null,
    @SerializedName("candidate_prediction")
    val candidatePrediction: CounterfactualPlannerPrediction? = null,
    @SerializedName("changed_features")
    val changedFeatures: List<CounterfactualChangedFeature> = emptyList(),
    @SerializedName("immutable_features")
    val immutableFeatures: List<String> = emptyList(),
    @SerializedName("input_prediction")
    val inputPrediction: CounterfactualPlannerPrediction? = null,
    @SerializedName("must_not_change")
    val mustNotChange: List<String> = emptyList(),
    @SerializedName("mutable_allowed")
    val mutableAllowed: List<String> = emptyList(),
    @SerializedName("recommended_candidate_id")
    val recommendedCandidateId: String? = null,
    @SerializedName("target_deltas")
    val targetDeltas: Map<String, Double> = emptyMap()
)

data class CounterfactualPlannerPrediction(
    @SerializedName("class_name")
    val className: String? = null,
    @SerializedName("probability_low_risk")
    val probabilityLowRisk: Double? = null
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

data class CounterfactualPrescriptivePlan(
    @SerializedName("action_steps")
    val actionSteps: List<String> = emptyList(),
    @SerializedName("clinical_scope")
    val clinicalScope: String? = null,
    @SerializedName("contraindication_flags")
    val contraindicationFlags: List<String> = emptyList(),
    @SerializedName("disclaimer")
    val disclaimer: String? = null,
    @SerializedName("generation_mode")
    val generationMode: String? = null,
    @SerializedName("goals")
    val goals: List<String> = emptyList(),
    @SerializedName("human_review_required")
    val humanReviewRequired: Boolean? = null,
    @SerializedName("missing_context")
    val missingContext: List<String> = emptyList(),
    @SerializedName("monitoring_plan")
    val monitoringPlan: List<String> = emptyList(),
    @SerializedName("policy_version")
    val policyVersion: String? = null,
    @SerializedName("provider")
    val provider: String? = null,
    @SerializedName("safety_notes")
    val safetyNotes: List<String> = emptyList(),
    @SerializedName("summary")
    val summary: String? = null
)
