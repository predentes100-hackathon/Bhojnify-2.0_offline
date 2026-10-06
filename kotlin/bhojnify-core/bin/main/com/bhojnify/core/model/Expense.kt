package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val date: String,
    val category: String,
    val amount: Double,
    val note: String = ""
)
