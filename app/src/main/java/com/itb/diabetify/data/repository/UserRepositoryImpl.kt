package com.itb.diabetify.data.repository

import com.itb.diabetify.data.remote.user.UserApiService
import com.itb.diabetify.data.remote.user.request.EditUserRequest
import com.itb.diabetify.domain.manager.TokenManager
import com.itb.diabetify.domain.manager.UserManager
import com.itb.diabetify.domain.model.User
import com.itb.diabetify.domain.repository.UserRepository
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import okio.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class UserRepositoryImpl(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager
): UserRepository {
    override suspend fun getToken(): String? {
        return tokenManager.getToken()
    }

    override suspend fun editUser(
        editUserRequest: EditUserRequest
    ): Resource<Unit> {
        return try {
            userApiService.editUser(editUserRequest)
            fetchUser()
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun fetchUser(): Resource<Unit> {
        return try {
            val response = userApiService.getUser()
            response.data?.let {
                userManager.saveUser(
                    User(
                        id = it.id,
                        name = it.name.orEmpty(),
                        email = it.email.orEmpty(),
                        gender = when (it.gender.orEmpty().lowercase()) {
                            "male" -> "Laki-laki"
                            "female" -> "Perempuan"
                            else -> it.gender.orEmpty()
                        },
                        dob = formatDobForDisplay(it.dob),
                    )
                )
            }
            return Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        } catch (e: ParseException) {
            Resource.Error("Format tanggal lahir tidak valid")
        }
    }

    override fun getUser(): Flow<User?> {
        return userManager.getUser()
    }

    private fun formatDobForDisplay(rawDob: String?): String {
        val trimmedDob = rawDob?.trim().orEmpty()
        if (trimmedDob.isBlank()) return ""

        val parsedDate = USER_DOB_INPUT_PATTERNS.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                }.parse(trimmedDob)
            }.getOrNull()
        } ?: throw ParseException("Unsupported DOB format", 0)

        return SimpleDateFormat(DISPLAY_DOB_PATTERN, Locale.US).format(parsedDate)
    }

    private companion object {
        val USER_DOB_INPUT_PATTERNS = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd"
        )
        const val DISPLAY_DOB_PATTERN = "dd/MM/yyyy"
    }
}
