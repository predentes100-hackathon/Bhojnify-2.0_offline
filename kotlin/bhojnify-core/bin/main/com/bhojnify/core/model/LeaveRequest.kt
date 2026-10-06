package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class LeaveStatus(val displayName: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    DECLINED("Declined");

    companion object {
        fun fromString(value: String): LeaveStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: PENDING
        }
    }
}

@Serializable
data class LeaveRequest(
    val id: String,
    val customerId: String? = null,
    val from: String,
    val to: String? = null,
    val reason: String = "",
    val status: LeaveStatus = LeaveStatus.PENDING
)
