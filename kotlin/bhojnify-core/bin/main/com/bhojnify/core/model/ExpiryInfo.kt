package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExpiryStatus {
    EXPIRED,
    TODAY,
    SOON,
    ACTIVE
}

@Serializable
data class ExpiryInfo(
    val daysRemaining: Long,
    val status: ExpiryStatus,
    val isExpired: Boolean,
    val isExpiringSoon: Boolean,
    val isExpiringToday: Boolean
)
