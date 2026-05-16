package com.itb.diabetify.data.remote.counterfactual.response

import com.google.gson.annotations.SerializedName

data class CounterfactualJobStatusResponse(
    @SerializedName("data")
    val data: CounterfactualJobStatusData,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class CounterfactualJobStatusData(
    @SerializedName("job_id")
    val jobId: String? = null,
    @SerializedName("job_status")
    val jobStatus: String? = null,
    @SerializedName("reason_code")
    val reasonCode: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("completed_at")
    val completedAt: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("error")
    val error: String? = null
)
