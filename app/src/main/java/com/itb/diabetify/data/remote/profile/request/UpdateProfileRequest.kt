package com.itb.diabetify.data.remote.profile.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest (
    @SerializedName("weight")
    val weight: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("hypertension")
    val hypertension: Boolean,
    @SerializedName("macrosomic_baby")
    val macrosomicBaby: Int,
    @SerializedName("cholesterol")
    val cholesterol: Boolean,
    @SerializedName("bloodline")
    val bloodline: Boolean,
    @SerializedName("smoking")
    val smoking: Int? = null,
    @SerializedName("age_of_smoking")
    val ageOfSmoking: Int? = null,
    @SerializedName("age_of_stop_smoking")
    val ageOfStopSmoking: Int? = null,
    @SerializedName("smoke_count")
    val smokeCount: Int? = null
)
