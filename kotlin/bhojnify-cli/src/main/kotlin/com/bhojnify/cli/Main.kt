package com.bhojnify.cli

import com.bhojnify.core.i18n.I18n
import com.bhojnify.core.i18n.Language
import com.bhojnify.core.model.*
import com.bhojnify.core.repository.JsonFileMessRepository
import com.bhojnify.core.service.DateUtils
import com.bhojnify.core.service.ReportCalculator
import com.bhojnify.core.state.MessStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.LocalDate

fun main(args: Array<String>) = runBlocking {
    val storageFile = File(System.getProperty("user.home"), ".bhojnify/messmate_state.json")
    val repository = JsonFileMessRepository(storageFile)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val stateManager = MessStateManager(repository, scope)

    println("==========================================================")
    println("           🍛 Bhojnify (MessMate) Kotlin CLI              ")
    println("       Campus-Kitchen & Mess Management Platform         ")
    println("==========================================================")

    val state = stateManager.state.value
    val lang = state.language

    fun t(key: String, vars: Map<String, Any> = emptyMap()) = I18n.translate(lang, key, vars)

    println("\n[1] Current Workspace Status:")
    println("  - Mess Name: ${state.profile.messName.ifEmpty { "Shivneri Mess" }}")
    println("  - Owner: ${state.profile.name.ifEmpty { "Ketan Patil" }}")
    println("  - Language: ${if (lang == Language.MR) "मराठी (Marathi)" else "English"}")
    println("  - Total Customers: ${state.customers.size}")
    println("  - Inventory Items: ${state.inventory.size}")
    println("  - Menu Items: ${state.menus.size}")

    println("\n[2] Customers & Plan Expiry:")
    val today = DateUtils.todayString()
    state.customers.forEach { customer ->
        val info = DateUtils.getExpiryInfo(customer.expiryDate, today)
        val days = info.daysRemaining
        val status = when {
            info.isExpired -> "[EXPIRED]"
            info.isExpiringSoon -> "[EXPIRING SOON - ${days}d left]"
            else -> "[ACTIVE - ${days}d left]"
        }
        println("  • ${customer.name.padEnd(18)} | ${customer.plan.padEnd(20)} | $status | Exp: ${customer.expiryDate}")
    }

    println("\n[3] Stock & Inventory Ledger:")
    state.inventory.forEach { item ->
        val low = item.quantity <= item.minimum
        val tag = if (low) "⚠️ LOW STOCK" else "✅ OK"
        println("  • ${item.name.padEnd(15)} : ${item.quantity} ${item.unit.padEnd(4)} (Min: ${item.minimum}) -> $tag")
    }

    println("\n[4] Financial Report & P&L Summary:")
    val report = ReportCalculator.calculate(
        payments = state.payments,
        expenses = state.expenses,
        inventory = state.inventory,
        attendanceCount = state.attendance.size
    )
    println("  • Total Revenue  : ₹${"%,.2f".format(report.totalRevenue)}")
    println("  • Total Expenses : ₹${"%,.2f".format(report.totalExpenses)}")
    println("  • Operating Margin: ₹${"%,.2f".format(report.operatingSurplus)} (${if (report.operatingSurplus >= 0) "Profit" else "Deficit"})")
    println("  • Stock Estimate : ₹${"%,.2f".format(report.stockValueEstimate)}")
    println("  • Meals Served   : ${report.mealsServedCount}")

    println("\n[5] Multi-Language i18n Verification:")
    println("  EN: ${I18n.translate(Language.EN, "goodMorning", mapOf("name" to "Ketan"))}")
    println("  MR: ${I18n.translate(Language.MR, "goodMorning", mapOf("name" to "केतन"))}")
    println("  EN: ${I18n.translate(Language.EN, "planExtendedNotice")}")
    println("  MR: ${I18n.translate(Language.MR, "planExtendedNotice")}")

    println("\n==========================================================")
    println("✨ Bhojnify Kotlin Core & CLI initialized successfully!")
    println("==========================================================")
}
