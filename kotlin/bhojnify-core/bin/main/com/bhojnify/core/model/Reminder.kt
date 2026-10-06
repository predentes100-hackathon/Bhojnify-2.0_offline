package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Reminder(
    val id: String,
    val title: String,
    val detail: String = "Inventory follow-up",
    val dueDate: String,
    val createdAt: String
)
