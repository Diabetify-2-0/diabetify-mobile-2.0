package com.itb.diabetify.data.remote.counterfactual

import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CounterfactualApiService {
    @POST("counterfactual")
    suspend fun submitCounterfactual(
        @Body request: CounterfactualRequest
    ): CounterfactualJobResponse

    @GET("counterfactual/job/{jobId}/status")
    suspend fun getCounterfactualJobStatus(
        @Path("jobId") jobId: String
    ): CounterfactualJobStatusResponse

    @GET("counterfactual/job/{jobId}/result")
    suspend fun getCounterfactualJobResult(
        @Path("jobId") jobId: String
    ): CounterfactualJobResultResponse
}
