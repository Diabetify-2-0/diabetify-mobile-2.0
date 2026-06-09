package com.itb.diabetify.data.remote.counterfactual.request

import com.google.gson.annotations.SerializedName

data class CounterfactualRequest(
    @SerializedName("model_version")
    val modelVersion: String = "xgb_v3",
    @SerializedName("instance")
    val instance: CounterfactualInstance,
    @SerializedName("constraints")
    val constraints: CounterfactualConstraints,
    @SerializedName("target")
    val target: CounterfactualTarget = CounterfactualTarget(),
    @SerializedName("generation")
    val generation: CounterfactualGeneration = CounterfactualGeneration()
)

data class CounterfactualInstance(
    @SerializedName("features")
    val features: CounterfactualFeatureSet
)

data class CounterfactualFeatureSet(
    @SerializedName("age")
    val age: Int,
    @SerializedName("smoking_status")
    val smokingStatus: Int,
    @SerializedName("is_cholesterol")
    val isCholesterol: Int,
    @SerializedName("is_macrosomic_baby")
    val isMacrosomicBaby: Int,
    @SerializedName("moderate_physical_activity_frequency")
    val moderatePhysicalActivityFrequency: Int,
    @SerializedName("is_bloodline")
    val isBloodline: Int,
    @SerializedName("brinkman_index")
    val brinkmanIndex: Int,
    @SerializedName("BMI")
    val bmi: Double,
    @SerializedName("is_hypertension")
    val isHypertension: Int
)

data class CounterfactualConstraints(
    @SerializedName("mutable_allowed")
    val mutableAllowed: List<String>
)

data class CounterfactualTarget(
    @SerializedName("target_class")
    val targetClass: String = "low_risk",
    @SerializedName("min_target_probability")
    val minTargetProbability: Double = 0.5
)

data class CounterfactualGeneration(
    @SerializedName("total_cfs")
    val totalCfs: Int = 1
)
