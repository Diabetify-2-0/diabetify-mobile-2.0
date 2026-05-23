package com.itb.diabetify.data.remote.profile.response

import com.google.gson.annotations.SerializedName

data class GetProfileResponse (
    @SerializedName("data")
    val data: ProfileData?,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)

data class ProfileData(
    @SerializedName("hypertension")
    val hypertension: Boolean? = null,
    @SerializedName("weight")
    val weight: Int? = null,
    @SerializedName("height")
    val height: Int? = null,
    @SerializedName("bmi")
    val bmi: Double? = null,
    @SerializedName("smoking")
    val smoking: Int? = null,
    @SerializedName("age_of_smoking")
    val ageOfSmoking: Int? = null,
    @SerializedName("age_of_stop_smoking")
    val ageOfStopSmoking: Int? = null,
    @SerializedName("macrosomic_baby")
    val macrosomicBaby: Int? = null,
    @SerializedName("cholesterol")
    val cholesterol: Boolean? = null,
    @SerializedName("bloodline")
    val bloodline: Boolean? = null,
    @SerializedName("smoke_count")
    val smokeCount: Int? = null
)
