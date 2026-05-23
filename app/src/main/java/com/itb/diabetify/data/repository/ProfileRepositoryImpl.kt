package com.itb.diabetify.data.repository

import com.itb.diabetify.data.remote.profile.ProfileApiService
import com.itb.diabetify.data.remote.profile.request.AddProfileRequest
import com.itb.diabetify.data.remote.profile.request.UpdateProfileRequest
import com.itb.diabetify.domain.manager.ProfileManager
import com.itb.diabetify.domain.manager.TokenManager
import com.itb.diabetify.domain.model.Profile
import com.itb.diabetify.domain.repository.ProfileRepository
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow
import okio.IOException
import retrofit2.HttpException

class ProfileRepositoryImpl(
    private val profileApiService: ProfileApiService,
    private val tokenManager: TokenManager,
    private val profileManager: ProfileManager
): ProfileRepository {
    override suspend fun getToken(): String? {
        return tokenManager.getToken()
    }

    override suspend fun addProfile(
        addProfileRequest: AddProfileRequest
    ): Resource<Unit> {
        return try {
            profileApiService.addProfile(addProfileRequest)
            fetchProfile()
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun updateProfile(
        updateProfileRequest: UpdateProfileRequest
    ): Resource<Unit> {
        return try {
            profileApiService.updateProfile(updateProfileRequest)
            fetchProfile()
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun fetchProfile(): Resource<Unit> {
        return try {
            val response = profileApiService.getProfile()
            response.data?.let { profile ->
                profileManager.saveProfile(
                    Profile(
                        cholesterol = profile.cholesterol ?: false,
                        bloodline = profile.bloodline ?: false,
                        hypertension = profile.hypertension ?: false,
                        weight = profile.weight ?: 0,
                        height = profile.height ?: 0,
                        bmi = profile.bmi ?: 0.0,
                        smoking = profile.smoking ?: 0,
                        smokeCount = profile.smokeCount ?: 0,
                        ageOfSmoking = profile.ageOfSmoking ?: 0,
                        ageOfStopSmoking = profile.ageOfStopSmoking ?: 0,
                        macrosomicBaby = profile.macrosomicBaby ?: 0
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

    override fun getProfile(): Flow<Profile?> {
        return profileManager.getProfile()
    }
}
