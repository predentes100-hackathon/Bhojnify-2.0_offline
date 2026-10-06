package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
data class InventoryItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String = "kg",
    val minimum: Double,
    val category: String = "Essentials"
) {
    val isLowStock: Boolean
        get() = quantity <= minimum
}
