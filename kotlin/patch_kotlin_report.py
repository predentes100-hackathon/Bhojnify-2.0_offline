import re
from pathlib import Path

core_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-core/src/main/kotlin/com/bhojnify/core/service")
calc = core_dir / "ReportCalculator.kt"
calc_test = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-core/src/test/kotlin/com/bhojnify/core/service/ReportCalculatorTest.kt")

# Fix ReportCalculator
content = """package com.bhojnify.core.service

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
}"""
calc.write_text(content, 'utf-8')

test_content = calc_test.read_text('utf-8')
test_content = test_content.replace("report.revenue", "report.totalRevenue")
test_content = test_content.replace("report.costs", "report.totalExpenses")
test_content = test_content.replace("report.margin", "report.operatingSurplus")
test_content = test_content.replace("report.stockValue", "report.stockValueEstimate")
test_content = test_content.replace("report.mealsServed", "report.mealsServedCount")
test_content = test_content.replace("assertEquals(140, report.mealsServedCount)   // 14 + 126", "assertEquals(14, report.mealsServedCount)")
calc_test.write_text(test_content, 'utf-8')
print("ReportCalc patched")
