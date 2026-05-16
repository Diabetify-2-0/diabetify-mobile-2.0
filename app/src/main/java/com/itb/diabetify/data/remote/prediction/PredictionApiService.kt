package com.itb.diabetify.data.remote.prediction

import com.itb.diabetify.data.remote.prediction.response.GetPredictionResponse
import com.itb.diabetify.data.remote.prediction.response.GetPredictionScoreResponse
import com.itb.diabetify.data.remote.prediction.response.PredictionJobResponse
import com.itb.diabetify.data.remote.prediction.response.PredictionJobStatusResponse
import com.itb.diabetify.data.remote.prediction.response.PredictionResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PredictionApiService {
    @POST("prediction")
    suspend fun predict(): PredictionJobResponse

    @GET("prediction/me/explanation")
    suspend fun explainPrediction(): PredictionResponse

    @GET("prediction/job/{jobId}/status")
    suspend fun getPredictionJobStatus(
        @Path("jobId") jobId: String
    ): PredictionJobStatusResponse

    @GET("prediction/me")
    suspend fun getPrediction(
        @Query("limit") limit: Int = 1,
    ): GetPredictionResponse

    @GET("prediction/me/date-range")
    suspend fun getPredictionByDate(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): GetPredictionResponse

    @GET("prediction/me/score")
    suspend fun getPredictionScoreByDate(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): GetPredictionScoreResponse
}
