package com.itb.diabetify.data.remote.user.response

import com.google.gson.annotations.SerializedName

data class GetUserResponse (
    @SerializedName("data")
    val data: UserData?,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)

data class UserData(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("dob")
    val dob: String? = null,
)
