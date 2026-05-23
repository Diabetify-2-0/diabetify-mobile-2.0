package com.itb.diabetify.data.remote.counterfactual.response

import com.google.gson.annotations.SerializedName

data class CounterfactualJobResponse(
    @SerializedName("data")
    val data: CounterfactualJobData? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class CounterfactualJobData(
    @SerializedName("job_id")
    val jobId: String? = null,
    @SerializedName("poll_url")
    val pollUrl: String? = null,
    @SerializedName("result_url")
    val resultUrl: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("submit_time")
    val submitTime: String? = null
)
