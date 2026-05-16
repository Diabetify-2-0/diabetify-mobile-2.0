package com.itb.diabetify.domain.repository

import com.itb.diabetify.data.remote.prediction.response.GetPredictionResponse
import com.itb.diabetify.data.remote.prediction.response.GetPredictionScoreResponse
import com.itb.diabetify.data.remote.prediction.response.PredictionJobResponse
import com.itb.diabetify.domain.manager.PredictionJobStatus
import com.itb.diabetify.domain.model.Prediction
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow

interface PredictionRepository {
    suspend fun getToken(): String?
    suspend fun predict(): Resource<Unit>
    suspend fun startPredictionJob(): Resource<PredictionJobResponse>
    suspend fun pollPredictionJob(jobId: String, pollingIntervalMs: Long = 2000L): Flow<PredictionJobStatus>
    suspend fun explainPrediction(): Resource<Unit>
    suspend fun fetchLatestPrediction(): Resource<Unit>
    suspend fun fetchPredictionByDate(startDate: String, endDate: String): Resource<GetPredictionResponse>
    suspend fun fetchPredictionScoreByDate(startDate: String, endDate: String): Resource<GetPredictionScoreResponse>
    fun getLatestPrediction(): Flow<Prediction?>
}
