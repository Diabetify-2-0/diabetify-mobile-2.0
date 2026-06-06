package com.itb.diabetify.data.remote.planner.request

import com.google.gson.annotations.SerializedName
import com.itb.diabetify.domain.model.planner.PlannerCheckInEntry

data class PlannerCheckInRequest(
    @SerializedName("id")
    val id: String,
    @SerializedName("goal_id")
    val goalId: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("value_text")
    val valueText: String,
    @SerializedName("note")
    val note: String,
    @SerializedName("created_at_millis")
    val createdAtMillis: Long
) {
    fun toDomain(): PlannerCheckInEntry {
        return PlannerCheckInEntry(
            id = id,
            goalId = goalId,
            type = type,
            label = label,
            valueText = valueText,
            note = note,
            createdAtMillis = createdAtMillis
        )
    }

    companion object {
        fun fromDomain(entry: PlannerCheckInEntry): PlannerCheckInRequest {
            return PlannerCheckInRequest(
                id = entry.id,
                goalId = entry.goalId,
                type = entry.type,
                label = entry.label,
                valueText = entry.valueText,
                note = entry.note,
                createdAtMillis = entry.createdAtMillis
            )
        }
    }
}
