package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class OwnerProfile(
    val name: String = "",
    val messName: String = "",
    val phone: String = "",
    val location: String = "",
    val email: String = ""
)
