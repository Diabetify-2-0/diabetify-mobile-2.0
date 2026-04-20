package com.itb.diabetify.data.repository

import android.annotation.SuppressLint
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    @SuppressLint("NewApi")
    override suspend fun fetchUser(): Resource<Unit> {
        return try {
            val response = userApiService.getUser()
            response.data?.let {
                userManager.saveUser(
                    User(
                        name = it.name,
                        email = it.email,
                        gender = when (it.gender.lowercase()) {
                            "male" -> "Laki-laki"
                            "female" -> "Perempuan"
                            else -> it.gender
                        },
                        dob = it.dob.let { dateString ->
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                            val dateTime = LocalDateTime.parse(dateString, formatter)
                            dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        },
                    )
                )
            }
            return Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override fun getUser(): Flow<User?> {
        return userManager.getUser()
    }
}
