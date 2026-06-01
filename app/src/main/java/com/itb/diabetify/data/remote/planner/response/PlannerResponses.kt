package com.itb.diabetify.data.remote.planner.response

import com.google.gson.annotations.SerializedName
import com.itb.diabetify.data.remote.planner.request.PlannerCheckInRequest
import com.itb.diabetify.data.remote.planner.request.PlannerGoalRequest

data class PlannerGoalResponse(
    @SerializedName("data")
    val data: PlannerGoalRequest?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
)

data class PlannerCheckInHistoryResponse(
    @SerializedName("data")
    val data: List<PlannerCheckInRequest>?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
)

data class PlannerCheckInStateResponse(
    @SerializedName("data")
    val data: Map<String, Long>?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
)
