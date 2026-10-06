package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class OwnerPolicies(
    val rules: String = "",
    val privacy: String = ""
)
