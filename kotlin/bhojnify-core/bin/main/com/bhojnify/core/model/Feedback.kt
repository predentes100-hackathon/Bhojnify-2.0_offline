package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Feedback(
    val id: String,
    val dish: String,
    val rating: Int,
    val note: String = "",
    val date: String
)
