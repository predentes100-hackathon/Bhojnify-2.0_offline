package com.bhojnify.core.service

import com.bhojnify.core.model.AttendanceRecord
import com.bhojnify.core.model.Expense
import com.bhojnify.core.model.InventoryItem
import com.bhojnify.core.model.Meal
import com.bhojnify.core.model.Payment
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReportCalculatorTest {

    @Test
    fun `test financial report metrics calculation`() {
        val payments = listOf(
            Payment(id = "1", amount = 3000.0, method = "UPI", note = "Fees", date = "2026-09-01"),
            Payment(id = "2", amount = 2500.0, method = "Cash", note = "Fees", date = "2026-09-02")
        )
        val expenses = listOf(
            Expense(id = "1", category = "Groceries", amount = 1500.0, note = "Rice & Dal", date = "2026-09-01"),
            Expense(id = "2", category = "Gas", amount = 1000.0, note = "Cylinder", date = "2026-09-02")
        )
        val inventory = listOf(
            InventoryItem(id = "1", name = "Rice", quantity = 25.0, unit = "kg", minimum = 10.0),
            InventoryItem(id = "2", name = "Dal", quantity = 10.0, unit = "kg", minimum = 5.0)
        )

        val report = ReportCalculator.calculate(payments, expenses, inventory, attendanceCount = 14)

        assertEquals(5500.0, report.totalRevenue)
        assertEquals(2500.0, report.totalExpenses)
        assertEquals(3000.0, report.operatingSurplus)
        assertEquals(3500.0, report.stockValueEstimate) // (25 + 10) * 100
        assertEquals(14, report.mealsServedCount)
    }

    @Test
    fun `test calculateForMonth filters by calendar year and month`() {
        val payments = listOf(
            Payment(id = "1", amount = 3000.0, method = "UPI", note = "Sept fees", date = "2026-09-05"),
            Payment(id = "2", amount = 4000.0, method = "Cash", note = "Oct fees", date = "2026-10-01"),
            Payment(id = "3", amount = 1500.0, method = "UPI", note = "Oct fees", date = "2026-10-15")
        )
        val expenses = listOf(
            Expense(id = "1", category = "Groceries", amount = 1000.0, date = "2026-09-10"),
            Expense(id = "2", category = "Gas", amount = 1200.0, date = "2026-10-05"),
            Expense(id = "3", category = "Vegetables", amount = 800.0, date = "2026-10-20")
        )
        val inventory = listOf(
            InventoryItem(id = "1", name = "Rice", quantity = 20.0, unit = "kg", minimum = 10.0)
        )
        val attendance = listOf(
            AttendanceRecord(id = "a1", date = "2026-09-10", meal = Meal.LUNCH, time = "13:00"),
            AttendanceRecord(id = "a2", date = "2026-10-01", meal = Meal.LUNCH, time = "13:00"),
            AttendanceRecord(id = "a3", date = "2026-10-02", meal = Meal.DINNER, time = "20:00")
        )

        val octReport = ReportCalculator.calculateForMonth("2026-10", payments, expenses, inventory, attendance)

        assertEquals(5500.0, octReport.totalRevenue) // 4000 + 1500
        assertEquals(2000.0, octReport.totalExpenses) // 1200 + 800
        assertEquals(3500.0, octReport.operatingSurplus)
        assertEquals(2000.0, octReport.stockValueEstimate) // 20 * 100
        assertEquals(2, octReport.mealsServedCount) // 2 attendance in October
    }

    @Test
    fun `test operating deficit when expenses exceed revenue`() {
        val payments = listOf(
            Payment(id = "p1", amount = 1500.0, method = "UPI", note = "Fees", date = "2026-11-01")
        )
        val expenses = listOf(
            Expense(id = "e1", category = "Rent", amount = 4500.0, date = "2026-11-02"),
            Expense(id = "e2", category = "Groceries", amount = 1000.0, date = "2026-11-03")
        )

        val report = ReportCalculator.calculate(payments, expenses, emptyList(), attendanceCount = 0)

        assertEquals(1500.0, report.totalRevenue)
        assertEquals(5500.0, report.totalExpenses)
        assertEquals(-4000.0, report.operatingSurplus)
        assertTrue(report.operatingSurplus < 0.0, "Operating surplus must be negative when expenses exceed revenue")

        val monthReport = ReportCalculator.calculateForMonth("2026-11", payments, expenses, emptyList(), emptyList())
        assertEquals(1500.0, monthReport.totalRevenue)
        assertEquals(5500.0, monthReport.totalExpenses)
        assertEquals(-4000.0, monthReport.operatingSurplus)
        assertTrue(monthReport.operatingSurplus < 0.0)
    }

    @Test
    fun `test empty month handling with zero transactions`() {
        val emptyReport = ReportCalculator.calculateForMonth(
            yearMonth = "2026-12",
            payments = emptyList(),
            expenses = emptyList(),
            inventory = emptyList(),
            attendance = emptyList()
        )

        assertEquals(0.0, emptyReport.totalRevenue)
        assertEquals(0.0, emptyReport.totalExpenses)
        assertEquals(0.0, emptyReport.operatingSurplus)
        assertEquals(0.0, emptyReport.stockValueEstimate)
        assertEquals(0, emptyReport.mealsServedCount)
    }

    @Test
    fun `test MoM revenue growth calculation edge cases`() {
        // Normal positive growth: +25.0%
        val positiveGrowth = ReportCalculator.calculateRevenueGrowth(currentRevenue = 2500.0, previousRevenue = 2000.0)
        assertEquals(25.0, positiveGrowth)

        // Normal negative growth: -25.0%
        val negativeGrowth = ReportCalculator.calculateRevenueGrowth(currentRevenue = 1500.0, previousRevenue = 2000.0)
        assertEquals(-25.0, negativeGrowth)

        // Zero growth (flat revenue): 0.0%
        val zeroGrowth = ReportCalculator.calculateRevenueGrowth(currentRevenue = 2000.0, previousRevenue = 2000.0)
        assertEquals(0.0, zeroGrowth)

        // Drop to zero revenue: -100.0%
        val totalDrop = ReportCalculator.calculateRevenueGrowth(currentRevenue = 0.0, previousRevenue = 2000.0)
        assertEquals(-100.0, totalDrop)

        // First month with revenue (no previous revenue baseline): null
        val firstMonth = ReportCalculator.calculateRevenueGrowth(currentRevenue = 3000.0, previousRevenue = 0.0)
        assertNull(firstMonth)

        // Both months zero revenue: 0.0
        val bothZero = ReportCalculator.calculateRevenueGrowth(currentRevenue = 0.0, previousRevenue = 0.0)
        assertEquals(0.0, bothZero)
    }
}
