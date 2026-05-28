package com.itb.diabetify.data.remote.planner

import com.itb.diabetify.data.remote.planner.request.PlannerCheckInRequest
import com.itb.diabetify.data.remote.planner.request.PlannerGoalRequest
import com.itb.diabetify.data.remote.planner.response.PlannerCheckInHistoryResponse
import com.itb.diabetify.data.remote.planner.response.PlannerCheckInStateResponse
import com.itb.diabetify.data.remote.planner.response.PlannerGoalHistoryResponse
import com.itb.diabetify.data.remote.planner.response.PlannerGoalResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PlannerApiService {
    @POST("planner/goals")
    suspend fun saveGoal(
        @Body request: PlannerGoalRequest
    ): PlannerGoalResponse

    @GET("planner/goals/active")
    suspend fun getLatestGoal(): PlannerGoalResponse

    @GET("planner/goals")
    suspend fun getGoalHistory(
        @Query("limit") limit: Int = 20
    ): PlannerGoalHistoryResponse

    @PATCH("planner/goals/{goalId}/archive")
    suspend fun archiveGoal(
        @Path("goalId") goalId: String
    ): PlannerGoalResponse

    @POST("planner/goals/{goalId}/check-ins")
    suspend fun recordCheckIn(
        @Path("goalId") goalId: String,
        @Body request: PlannerCheckInRequest
    )

    @GET("planner/goals/{goalId}/check-ins")
    suspend fun getCheckInHistory(
        @Path("goalId") goalId: String,
        @Query("limit") limit: Int = 80
    ): PlannerCheckInHistoryResponse

    @GET("planner/goals/{goalId}/check-ins/last")
    suspend fun getLastCheckIns(
        @Path("goalId") goalId: String
    ): PlannerCheckInStateResponse
}
