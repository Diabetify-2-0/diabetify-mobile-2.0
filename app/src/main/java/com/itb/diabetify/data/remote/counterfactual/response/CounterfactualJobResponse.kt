package com.itb.diabetify.data.remote.counterfactual.response

import com.google.gson.annotations.SerializedName

data class CounterfactualJobResponse(
    @SerializedName("data")
    val data: CounterfactualJobData,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)

data class CounterfactualJobData(
    @SerializedName("job_id")
    val jobId: String,
    @SerializedName("poll_url")
    val pollUrl: String,
    @SerializedName("result_url")
    val resultUrl: String? = null,
    @SerializedName("status")
    val status: String,
    @SerializedName("submit_time")
    val submitTime: String
)
