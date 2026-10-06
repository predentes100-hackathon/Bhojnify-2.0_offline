package com.bhojnify.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    OWNER;

    override fun toString(): String = name.lowercase()
}
