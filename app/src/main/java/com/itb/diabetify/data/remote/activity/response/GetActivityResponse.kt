package com.itb.diabetify.data.remote.activity.response

import com.google.gson.annotations.SerializedName

data class GetActivityResponse (
    @SerializedName("data")
    val data: ActivityType?,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)

data class ActivityType(
    @SerializedName("workout")
    val workout: List<ActivityData>
)

data class ActivityData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("activity_type")
    val activityType: String,
    @SerializedName("activity_date")
    val activityDate: String,
    @SerializedName("value")
    val value: Int
)
