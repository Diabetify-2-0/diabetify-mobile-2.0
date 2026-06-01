package com.itb.diabetify.e2e.repository

import com.itb.diabetify.data.remote.profile.request.AddProfileRequest
import com.itb.diabetify.data.remote.profile.request.UpdateProfileRequest
import com.itb.diabetify.domain.model.Profile
import com.itb.diabetify.domain.repository.ProfileRepository
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeProfileRepository @Inject constructor() : ProfileRepository {

    var shouldFailAddProfile = false
    var shouldFailUpdateProfile = false
    var shouldFailFetchProfile = false
    var shouldFailGetProfile = false

    override suspend fun fetchProfile(): Resource<Unit> {
        if (shouldFailFetchProfile) {
            return Resource.Error("404 - Profile not found")
        }
        return Resource.Success(Unit)
    }

    override fun getProfile(): Flow<Profile?> {
        if (shouldFailGetProfile) {
            return flowOf(null)
        }
        return flowOf(
            Profile(
                cholesterol = true,
                bloodline = true,
                hypertension = true,
                weight = 80,
                height = 180,
                bmi = 24.7,
                smoking = 0,
                smokeCount = 0,
                ageOfSmoking = 15,
                ageOfStopSmoking = 16,
                macrosomicBaby = 0
            )
        )
    }

    override suspend fun addProfile(addProfileRequest: AddProfileRequest): Resource<Unit> {
        if (shouldFailAddProfile) {
            return Resource.Error("Failed to add profile")
        }
        return Resource.Success(Unit)
    }

    override suspend fun updateProfile(updateProfileRequest: UpdateProfileRequest): Resource<Unit> {
        if (shouldFailUpdateProfile) {
            return Resource.Error("Failed to update profile")
        }
        return Resource.Success(Unit)
    }

    override suspend fun getToken(): String? {
        return null
    }
}
