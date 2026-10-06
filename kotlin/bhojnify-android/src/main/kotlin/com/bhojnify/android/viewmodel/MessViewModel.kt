package com.bhojnify.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhojnify.core.i18n.I18n
import com.bhojnify.core.i18n.Language
import com.bhojnify.core.model.*
import com.bhojnify.core.repository.JsonFileMessRepository
import com.bhojnify.core.repository.MessRepository
import com.bhojnify.core.service.DateUtils
import com.bhojnify.core.service.FinancialReportSummary
import com.bhojnify.core.service.ReportCalculator
import com.bhojnify.core.state.MessStateManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File

class MessViewModel(application: Application) : AndroidViewModel(application) {

    private val storageFile = File(application.filesDir, "messmate_state.json")
    private val repository: MessRepository = JsonFileMessRepository(storageFile)
    val stateManager: MessStateManager = MessStateManager(repository, viewModelScope)

    val state: StateFlow<MessState> = stateManager.state

    val reportSummary: StateFlow<FinancialReportSummary> = state.map { s ->
        ReportCalculator.calculate(
            payments = s.payments,
            expenses = s.expenses,
            inventory = s.inventory,
            attendanceCount = s.attendance.size
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportCalculator.calculate(emptyList(), emptyList(), emptyList(), 0)
    )

    fun t(key: String, variables: Map<String, Any> = emptyMap()): String {
        return I18n.translate(state.value.language, key, variables)
    }

    fun localized(value: String): String {
        return I18n.localizedValue(state.value.language, value)
    }

    fun setLanguage(language: Language) = stateManager.setLanguage(language)
    fun completeOwnerSetup(profile: OwnerProfile) = stateManager.completeOwnerSetup(profile)
    fun updatePolicies(policies: OwnerPolicies) = stateManager.updatePolicies(policies)
    fun markAttendance(meal: Meal, verified: Boolean = true, method: String = "Biometric + GPS") =
        stateManager.markAttendance(meal, verified, method)

    fun addCustomer(
        name: String,
        plan: String,
        joiningDate: String,
        expiryDate: String,
        phone: String,
        paymentStatus: CustomerPaymentStatus = CustomerPaymentStatus.PAID,
        imageUri: String? = null
    ) = stateManager.addCustomer(name, plan, joiningDate, expiryDate, phone, paymentStatus, imageUri)

    fun markCustomerPaid(id: String) = stateManager.markCustomerPaid(id)
    fun renewCustomerPlan(id: String) = stateManager.renewCustomerPlan(id)

    fun addLeave(customerId: String, from: String, to: String?, reason: String) =
        stateManager.addLeave(customerId, from, to, reason)

    fun updateLeaveStatus(requestId: String, status: LeaveStatus) =
        stateManager.updateLeaveStatus(requestId, status)

    fun updateLeave(requestId: String, from: String, to: String?, reason: String, customerId: String? = null) =
        stateManager.updateLeave(requestId, from, to, reason, customerId)

    fun deleteLeave(requestId: String) = stateManager.deleteLeave(requestId)
    fun completeLeave(requestId: String, returnDate: String) =
        stateManager.completeLeave(requestId, returnDate)

    fun addInventory(name: String, quantity: Double, unit: String, minimum: Double, category: String = "Essentials") =
        stateManager.addInventory(name, quantity, unit, minimum, category)

    fun removeInventory(id: String) = stateManager.removeInventory(id)

    fun addMenu(day: String, meal: Meal, dish: String, note: String) =
        stateManager.addMenu(day, meal, dish, note)

    fun addStaff(name: String, role: String, phone: String, salary: Double) =
        stateManager.addStaff(name, role, phone, salary)

    fun addExpense(category: String, amount: Double, note: String) =
        stateManager.addExpense(category, amount, note)

    fun addPayment(amount: Double, method: String, note: String) =
        stateManager.addPayment(amount, method, note)

    fun addReminder(title: String, daysFromNow: Long, detail: String = "Inventory follow-up") =
        stateManager.addReminder(title, daysFromNow, detail)

    fun resolveReminder(id: String) = stateManager.resolveReminder(id)
    fun replaceState(state: MessState) = stateManager.replaceState(state)

    fun addFeedback(dish: String, rating: Int, note: String) =
        stateManager.addFeedback(dish, rating, note)
}
