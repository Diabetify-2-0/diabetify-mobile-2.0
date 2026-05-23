package com.itb.diabetify.domain.usecases.profile

import com.itb.diabetify.data.remote.profile.request.UpdateProfileRequest
import com.itb.diabetify.domain.model.profile.UpdateProfileResult
import com.itb.diabetify.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(
        weight: Int,
        height: Int,
        hypertension: Boolean,
        macrosomicBaby: Int,
        bloodline: Boolean,
        cholesterol: Boolean,
        smoking: Int? = null,
        ageOfSmoking: Int? = null,
        ageOfStopSmoking: Int? = null,
        smokeCount: Int? = null,
    ): UpdateProfileResult {
        val weightError: String? = if (weight < 30 || weight > 300) "Berat badan tidak valid" else null
        val heightError: String? = if (height < 100 || height > 250) "Tinggi badan tidak valid" else null
        val macrosomicBabyError: String? = if (macrosomicBaby < 0 || macrosomicBaby > 2) "Status bayi makrosomia tidak valid" else null
        val smokingError: String? = if (smoking != null && smoking !in 0..2) "Status merokok tidak valid" else null
        val ageOfSmokingError: String? = if (ageOfSmoking != null && ageOfSmoking !in 0..80) "Usia mulai merokok tidak valid" else null
        val ageOfStopSmokingError: String? = if (ageOfStopSmoking != null && ageOfStopSmoking !in 0..80) "Usia berhenti merokok tidak valid" else null
        val smokeCountError: String? = if (smokeCount != null && smokeCount !in 0..60) "Jumlah rokok tidak valid" else null

        if (weightError != null) {
            return UpdateProfileResult(
                weightError = weightError
            )
        }

        if (heightError != null) {
            return UpdateProfileResult(
                heightError = heightError
            )
        }

        if (macrosomicBabyError != null) {
            return UpdateProfileResult(
                macrosomicBabyError = macrosomicBabyError
            )
        }

        if (smokingError != null) {
            return UpdateProfileResult(
                smokingError = smokingError
            )
        }

        if (ageOfSmokingError != null) {
            return UpdateProfileResult(
                ageOfSmokingError = ageOfSmokingError
            )
        }

        if (ageOfStopSmokingError != null) {
            return UpdateProfileResult(
                ageOfStopSmokingError = ageOfStopSmokingError
            )
        }

        if (smokeCountError != null) {
            return UpdateProfileResult(
                smokeCountError = smokeCountError
            )
        }

        val updateProfileResult = UpdateProfileRequest(
            weight = weight,
            height = height,
            hypertension = hypertension,
            macrosomicBaby = macrosomicBaby,
            bloodline = bloodline,
            cholesterol = cholesterol,
            smoking = smoking,
            ageOfSmoking = ageOfSmoking,
            ageOfStopSmoking = ageOfStopSmoking,
            smokeCount = smokeCount
        )

        return UpdateProfileResult(
            result = repository.updateProfile(updateProfileResult)
        )
    }
}
