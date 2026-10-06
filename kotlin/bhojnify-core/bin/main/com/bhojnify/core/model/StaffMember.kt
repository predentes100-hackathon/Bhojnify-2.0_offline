package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class StaffMember(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val salary: Double
)
