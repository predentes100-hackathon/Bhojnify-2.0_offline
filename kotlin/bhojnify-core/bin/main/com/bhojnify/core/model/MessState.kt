package com.bhojnify.core.model

import com.bhojnify.core.i18n.Language
import kotlinx.serialization.Serializable

@Serializable
data class MessState(
    val role: Role = Role.OWNER,
    val language: Language = Language.EN,
    val onboardingComplete: Boolean = false,
    val credits: Int = 18,
    val expiresOn: String = "2026-09-18",
    val profile: OwnerProfile = OwnerProfile(),
    val policies: OwnerPolicies = OwnerPolicies(),
    val attendance: List<AttendanceRecord> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val leaves: List<LeaveRequest> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val menus: List<MenuItem> = emptyList(),
    val staff: List<StaffMember> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val feedback: List<Feedback> = emptyList(),
    val reminders: List<Reminder> = emptyList()
) {
    companion object {
        fun defaultInitialState(): MessState {
            return MessState(
                role = Role.OWNER,
                language = Language.EN,
                onboardingComplete = false,
                credits = 18,
                expiresOn = "2026-09-18",
                profile = OwnerProfile(
                    name = "",
                    messName = "",
                    phone = "",
                    location = "",
                    email = ""
                ),
                policies = OwnerPolicies(rules = "", privacy = ""),
                attendance = emptyList(),
                customers = emptyList(),
                leaves = emptyList(),
                payments = emptyList(),
                inventory = emptyList(),
                menus = emptyList(),
                staff = emptyList(),
                expenses = emptyList(),
                feedback = emptyList(),
                reminders = emptyList()
            )
        }
    }
}
