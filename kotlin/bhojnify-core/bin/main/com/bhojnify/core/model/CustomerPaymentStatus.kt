package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class CustomerPaymentStatus(val displayName: String) {
    PAID("Paid"),
    UNPAID("Unpaid");

    companion object {
        fun fromString(value: String): CustomerPaymentStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: UNPAID
        }
    }
}
