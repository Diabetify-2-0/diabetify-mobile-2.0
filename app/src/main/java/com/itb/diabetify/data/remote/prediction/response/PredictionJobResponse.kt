package com.itb.diabetify.data.remote.prediction.response

import com.google.gson.annotations.SerializedName

data class PredictionJobResponse (
    @SerializedName("data")
    val data: PredictionJobData? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class PredictionJobData(
    @SerializedName("job_id")
    val jobId: String? = null,
    @SerializedName("poll_url")
    val pollUrl: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("submit_time")
    val submitTime: String? = null
)
