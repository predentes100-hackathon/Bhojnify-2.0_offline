package com.bhojnify.core.service

import com.bhojnify.core.model.AttendanceRecord
import com.bhojnify.core.model.Expense
import com.bhojnify.core.model.InventoryItem
import com.bhojnify.core.model.Payment
import kotlinx.serialization.Serializable

@Serializable
data class FinancialReportSummary(
    val totalRevenue: Double,
    val totalExpenses: Double,
    val operatingSurplus: Double,
    val stockValueEstimate: Double,
    val mealsServedCount: Int
)

object ReportCalculator {
    fun calculate(
        payments: List<Payment>,
        expenses: List<Expense>,
        inventory: List<InventoryItem>,
        attendanceCount: Int
    ): FinancialReportSummary {
        val revenue = payments.sumOf { it.amount }
        val spend = expenses.sumOf { it.amount }
        val surplus = revenue - spend
        val stockValue = inventory.sumOf { it.quantity * 100.0 }

        return FinancialReportSummary(
            totalRevenue = revenue,
            totalExpenses = spend,
            operatingSurplus = surplus,
            stockValueEstimate = stockValue,
            mealsServedCount = attendanceCount
        )
    }

    fun calculateForMonth(
        yearMonth: String,
        payments: List<Payment>,
        expenses: List<Expense>,
        inventory: List<InventoryItem>,
        attendance: List<AttendanceRecord>
    ): FinancialReportSummary {
        val filteredPayments = payments.filter { it.date.startsWith(yearMonth) }
        val filteredExpenses = expenses.filter { it.date.startsWith(yearMonth) }
        val filteredAttendance = attendance.count { it.date.startsWith(yearMonth) }
        return calculate(
            payments = filteredPayments,
            expenses = filteredExpenses,
            inventory = inventory,
            attendanceCount = filteredAttendance
        )
    }

    fun calculateRevenueGrowth(currentRevenue: Double, previousRevenue: Double): Double? {
        return when {
            previousRevenue > 0.0 -> ((currentRevenue - previousRevenue) / previousRevenue) * 100.0
            currentRevenue > 0.0 -> null // First recorded revenue month
            else -> 0.0
        }
    }
}