package com.bhojnify.core.service

import com.bhojnify.core.model.Expense
import com.bhojnify.core.model.InventoryItem
import com.bhojnify.core.model.Payment
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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
}
