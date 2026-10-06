package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRecord(
    val id: String,
    val date: String,
    val meal: Meal,
    val time: String,
    val verified: Boolean = true,
    val method: String = "Biometric + GPS"
)
