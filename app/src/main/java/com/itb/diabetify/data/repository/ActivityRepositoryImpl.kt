package com.itb.diabetify.data.repository

import android.annotation.SuppressLint
import com.itb.diabetify.data.remote.activity.ActivityApiService
import com.itb.diabetify.data.remote.activity.request.AddActivityRequest
import com.itb.diabetify.data.remote.activity.request.UpdateActivityRequest
import com.itb.diabetify.domain.manager.ActivityManager
import com.itb.diabetify.domain.manager.TokenManager
import com.itb.diabetify.domain.model.Activity
import com.itb.diabetify.domain.repository.ActivityRepository
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow
import okio.IOException
import retrofit2.HttpException

class ActivityRepositoryImpl(
    private val activityApiService: ActivityApiService,
    private val tokenManager: TokenManager,
    private val activityManager: ActivityManager
): ActivityRepository {
    override suspend fun getToken(): String? {
        return tokenManager.getToken()
    }

    override suspend fun addActivity(
        addActivityRequest: AddActivityRequest
    ): Resource<Unit> {
        return try {
            activityApiService.addActivity(addActivityRequest)
            fetchActivityToday()
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun updateActivity(
        activityId: Int,
        updateActivityRequest: UpdateActivityRequest
    ): Resource<Unit> {
        return try {
            activityApiService.updateActivity(activityId, updateActivityRequest)
            fetchActivityToday()
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    @SuppressLint("NewApi")
    override suspend fun fetchActivityToday(): Resource<Unit> {
        return try {
            val currentDate = java.time.LocalDate.now()
            val startDate = currentDate.toString()
            val response = activityApiService.getActivityByDate(startDate, startDate)
            response.data?.let { activities ->
                val smokingActivity = activities.smoke.firstOrNull()
                val workoutActivity = activities.workout.firstOrNull()

                activityManager.saveActivity(
                    Activity(
                        smokingId = smokingActivity?.id,
                        workoutId = workoutActivity?.id,
                        smokingValue = smokingActivity?.value ?: 0,
                        workoutValue = workoutActivity?.value ?: 0
                    )
                )
            }
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override fun getActivityToday(): Flow<Activity?> {
        return activityManager.getActivityToday()
    }
}
