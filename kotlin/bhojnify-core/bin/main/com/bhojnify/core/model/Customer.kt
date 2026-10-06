package com.bhojnify.core.model

import kotlinx.serialization.Serializable

object MessPlans {
    const val MONTHLY_UNLIMITED = "Monthly Unlimited (2 Meals)"
    const val MONTHLY_LUNCH_ONLY = "Monthly Lunch Only"
    const val MONTHLY_DINNER_ONLY = "Monthly Dinner Only"
    const val FIFTEEN_DAY_FLEXI = "15-Day Flexi Plan"
    const val DAILY_PASS = "Daily / Per Meal"
    const val CUSTOM_PLAN = "Custom Plan"

    val ALL = listOf(
        MONTHLY_UNLIMITED,
        MONTHLY_LUNCH_ONLY,
        MONTHLY_DINNER_ONLY,
        FIFTEEN_DAY_FLEXI,
        DAILY_PASS,
        CUSTOM_PLAN
    )
}

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val plan: String,
    val joiningDate: String,
    val expiryDate: String,
    val paymentStatus: CustomerPaymentStatus = CustomerPaymentStatus.PAID,
    val phone: String,
    val imageUri: String? = null
)
