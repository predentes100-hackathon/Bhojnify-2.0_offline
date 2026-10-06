package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Meal(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner");

    companion object {
        fun fromString(value: String): Meal {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: LUNCH
        }
    }
}
