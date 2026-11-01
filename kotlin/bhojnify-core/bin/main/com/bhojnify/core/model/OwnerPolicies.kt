package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class OwnerPolicies(
    val rules: String = "",
    val privacy: String = "",
    val maxPlanValidityDays: Int = 40,
    val maxLeaveDays: Int = 10
)
