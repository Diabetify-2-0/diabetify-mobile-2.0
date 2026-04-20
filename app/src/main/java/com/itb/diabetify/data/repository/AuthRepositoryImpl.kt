package com.itb.diabetify.data.repository

import com.itb.diabetify.data.remote.auth.AuthApiService
import com.itb.diabetify.data.remote.auth.request.ChangePasswordRequest
import com.itb.diabetify.data.remote.auth.request.CreateAccountRequest
import com.itb.diabetify.data.remote.auth.request.LoginRequest
import com.itb.diabetify.data.remote.auth.request.SendVerificationRequest
import com.itb.diabetify.data.remote.auth.request.VerifyOtpRequest
import com.itb.diabetify.domain.manager.TokenManager
import com.itb.diabetify.domain.repository.AuthRepository
import com.itb.diabetify.util.Resource
import okio.IOException
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
): AuthRepository {
    override suspend fun createAccount(
        createAccountRequest: CreateAccountRequest
    ): Resource<Unit> {
        return try {
            authApiService.createAccount(createAccountRequest)
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            if (e.code() == 400) {
                Resource.Error("Akun dengan email ini sudah terdaftar")
            } else {
                Resource.Error("${e.message}")
            }
        }
    }

    override suspend fun sendVerification(
        sendVerificationRequest: SendVerificationRequest,
        type: String
    ): Resource<Unit> {
        return try {
            when (type) {
                "register" -> {
                    authApiService.sendVerification(sendVerificationRequest)
                }
                "reset-password" -> {
                    authApiService.sendResetPasswordVerification(sendVerificationRequest)
                }
                else -> {
                    return Resource.Error("Invalid type")
                }
            }
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            if (e.code() == 400) {
                Resource.Error("Email tidak terdaftar")
            } else {
                Resource.Error("${e.message}")
            }
        }
    }

    override suspend fun verifyOtp(
        verifyOtpRequest: VerifyOtpRequest
    ): Resource<Unit> {
        return try {
            authApiService.verifyOtp(verifyOtpRequest)
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Resource.Error("Kode OTP salah atau sudah kadaluarsa")
            } else {
                Resource.Error("${e.message}")
            }
        }
    }

    override suspend fun login(
        loginRequest: LoginRequest
    ): Resource<Unit> {
        return try {
            val response = authApiService.login(loginRequest)
            tokenManager.saveToken(response.data.toString())
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Resource.Error("Password salah")
            } else if (e.code() == 404) {
                Resource.Error("Akun tidak ditemukan")
            } else {
                Resource.Error("${e.message}")
            }
        }
    }

    override suspend fun changePassword(
        changePasswordRequest: ChangePasswordRequest
    ): Resource<Unit> {
        return try {
            authApiService.changePassword(changePasswordRequest)
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            if (e.code() == 400) {
                Resource.Error("Kode OTP salah atau sudah kadaluarsa")
            } else {
                Resource.Error("${e.message}")
            }
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            tokenManager.clearToken()
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }
}
