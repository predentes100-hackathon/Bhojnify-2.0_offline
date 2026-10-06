package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String,
    val date: String,
    val amount: Double,
    val method: String = "UPI",
    val note: String = ""
)
