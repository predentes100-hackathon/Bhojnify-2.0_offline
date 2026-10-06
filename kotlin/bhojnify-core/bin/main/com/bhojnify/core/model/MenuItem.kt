package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val id: String,
    val day: String,
    val meal: Meal,
    val dish: String,
    val note: String = ""
)
