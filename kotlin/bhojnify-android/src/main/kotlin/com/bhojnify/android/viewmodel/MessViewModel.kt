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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MessViewModel(application: Application) : AndroidViewModel(application) {

    private val storageFile = File(application.filesDir, "messmate_state.json")
    private val repository: MessRepository = JsonFileMessRepository(storageFile)
    val stateManager: MessStateManager = MessStateManager(repository, viewModelScope)

    val state: StateFlow<MessState> = stateManager.state

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _selectedReportMonth = MutableStateFlow(DateUtils.currentYearMonth())
    val selectedReportMonth: StateFlow<String> = _selectedReportMonth.asStateFlow()

    init {
        viewModelScope.launch {
            _isReady.value = true
        }
    }

    val reportSummary: StateFlow<FinancialReportSummary> = combine(state, _selectedReportMonth) { s, month ->
        ReportCalculator.calculateForMonth(
            yearMonth = month,
            payments = s.payments,
            expenses = s.expenses,
            inventory = s.inventory,
            attendance = s.attendance
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportCalculator.calculate(emptyList(), emptyList(), emptyList(), 0)
    )

    val monthlyRevenueGrowth: StateFlow<Double?> = combine(state, _selectedReportMonth) { s, month ->
        val currentRevenue = s.payments.filter { it.date.startsWith(month) }.sumOf { it.amount }
        val prevMonth = DateUtils.previousYearMonth(month)
        val prevRevenue = s.payments.filter { it.date.startsWith(prevMonth) }.sumOf { it.amount }

        ReportCalculator.calculateRevenueGrowth(currentRevenue, prevRevenue)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val selectedMonthExpenseBreakdown: StateFlow<Map<String, Double>> = combine(state, _selectedReportMonth) { s, month ->
        s.expenses
            .filter { it.date.startsWith(month) }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val selectedMonthPaymentBreakdown: StateFlow<Map<String, Double>> = combine(state, _selectedReportMonth) { s, month ->
        s.payments
            .filter { it.date.startsWith(month) }
            .groupBy { it.method }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    fun setReportMonth(yearMonth: String) {
        _selectedReportMonth.value = yearMonth
    }

    fun selectPreviousReportMonth() {
        _selectedReportMonth.value = DateUtils.previousYearMonth(_selectedReportMonth.value)
    }

    fun selectNextReportMonth() {
        _selectedReportMonth.value = DateUtils.nextYearMonth(_selectedReportMonth.value)
    }

    fun selectCurrentReportMonth() {
        _selectedReportMonth.value = DateUtils.currentYearMonth()
    }

    fun t(key: String, variables: Map<String, Any> = emptyMap()): String {
        return I18n.translate(state.value.language, key, variables)
    }

    fun localized(value: String): String {
        return I18n.localizedValue(state.value.language, value)
    }

    fun setLanguage(language: Language) = stateManager.setLanguage(language)
    fun completeOwnerSetup(profile: OwnerProfile) = stateManager.completeOwnerSetup(profile)
    fun updatePolicies(policies: OwnerPolicies) = stateManager.updatePolicies(policies)
    fun markAttendance(
        meal: Meal,
        verified: Boolean = false,
        method: String = "Manual",
        customerId: String? = null
    ) = stateManager.markAttendance(meal, verified, method, customerId)

    fun markAttendanceBatch(customerIds: List<String>, meal: Meal, method: String = "Manual") =
        stateManager.markAttendanceBatch(customerIds, meal, method)

    fun unmarkAttendance(customerId: String, meal: Meal) =
        stateManager.unmarkAttendance(customerId, meal)

    fun addCustomer(
        name: String,
        plan: String,
        joiningDate: String,
        expiryDate: String,
        phone: String,
        paymentStatus: CustomerPaymentStatus = CustomerPaymentStatus.PAID,
        imageUri: String? = null,
        initialAmount: Double? = null
    ) = stateManager.addCustomer(name, plan, joiningDate, expiryDate, phone, paymentStatus, imageUri, initialAmount)

    fun updateCustomer(id: String, customer: Customer) = stateManager.updateCustomer(id) { customer }
    fun updateCustomer(id: String, transform: (Customer) -> Customer) = stateManager.updateCustomer(id, transform)

    fun markCustomerPaid(id: String, amount: Double? = null) = stateManager.markCustomerPaid(id, amount)
    fun renewCustomerPlan(id: String, amount: Double? = null) = stateManager.renewCustomerPlan(id, amount = amount)
    fun deleteCustomer(id: String) = stateManager.deleteCustomer(id)

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

    fun addPayment(amount: Double, method: String, note: String, customerId: String? = null) =
        stateManager.addPayment(amount, method, note, customerId)

    fun addReminder(title: String, daysFromNow: Long, detail: String = "Inventory follow-up") =
        stateManager.addReminder(title, daysFromNow, detail)

    fun resolveReminder(id: String) = stateManager.resolveReminder(id)
    fun replaceState(state: MessState) = stateManager.replaceState(state)

    fun addFeedback(dish: String, rating: Int, note: String) =
        stateManager.addFeedback(dish, rating, note)
}
