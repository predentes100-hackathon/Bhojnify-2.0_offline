package com.bhojnify.core.i18n

import kotlinx.serialization.Serializable

@Serializable
enum class Language(val code: String, val displayName: String) {
    EN("en", "English"),
    MR("mr", "मराठी");

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN
        }
    }
}
