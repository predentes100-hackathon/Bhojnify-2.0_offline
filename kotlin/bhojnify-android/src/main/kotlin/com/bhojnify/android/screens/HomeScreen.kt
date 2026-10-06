package com.bhojnify.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import com.bhojnify.core.i18n.Language
import com.bhojnify.core.service.DateUtils
import java.time.LocalDate

@Composable
fun HomeScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()

    val profile = state.profile
    val inventory = state.inventory
    val attendance = state.attendance
    val expenses = state.expenses
    val payments = state.payments
    val reminders = state.reminders
    val customers = state.customers

    val lowStock = inventory.filter { it.quantity <= it.minimum }
    val revenue = payments.sumOf { it.amount }
    val spend = expenses.sumOf { it.amount }
    val currentDate = LocalDate.now().toString()
    val dueReminders = reminders.filter { it.dueDate <= currentDate }

    // Note: Java default Locale mapping would be needed for exact formatting,
    // Using a simpler string for now, or just let i18n handle what it can
    val todayLabel = LocalDate.now().toString()

    val expiringCustomers = reactToCustomers(customers)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Header(
            eyebrow = "${viewModel.t("ownerWorkspace")} · $todayLabel",
            title = viewModel.t("goodMorning", mapOf("name" to (profile.name.ifEmpty { viewModel.t("owner") }))),
            subtitle = profile.messName.ifEmpty { viewModel.t("herePulse") },
            onActionClick = { navController.navigate(Routes.PROFILE) }
        )

        if (expiringCustomers.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.accent)
                    .border(1.dp, colors.accentForeground, RoundedCornerShape(20.dp))
                    .clickable { navController.navigate(Routes.CUSTOMERS) }
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accentForeground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Timer, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${viewModel.t("expiryAlerts")} (${expiringCustomers.size})",
                            color = colors.accentForeground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = viewModel.t("membersExpiringBanner", mapOf("count" to expiringCustomers.size, "plural" to if (expiringCustomers.size == 1) "" else "s")),
                            color = colors.accentForeground.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = colors.accentForeground, modifier = Modifier.size(18.dp))
                }
            }
        }

        if (dueReminders.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, colors.accent, RoundedCornerShape(20.dp))
                    .background(colors.accent)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accentForeground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.t("dueReminders"),
                            color = colors.accentForeground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = viewModel.t("followUpsNeedAttention", mapOf("count" to dueReminders.size, "plural" to if (dueReminders.size == 1) "" else "s")),
                            color = colors.accentForeground.copy(alpha = 0.78f),
                            fontSize = 12.sp
                        )
                    }
                }

                dueReminders.forEach { reminder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = reminder.title, color = colors.accentForeground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${reminder.detail} · ${reminder.dueDate}", color = colors.accentForeground.copy(alpha = 0.78f), fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.accentForeground)
                                .clickable { viewModel.resolveReminder(reminder.id) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(text = viewModel.t("resolve"), color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Owner Hero
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.foreground)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = viewModel.t("todayAtGlance").uppercase(), color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Text(text = viewModel.t("keepKitchenMoving"), color = colors.card, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text(text = viewModel.t("operationsSteady"), color = colors.card.copy(alpha = 0.75f), fontSize = 12.sp)
            }
            Icon(Icons.Default.Restaurant, null, tint = colors.accent, modifier = Modifier.size(46.dp))
        }

        // Stats Row
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = viewModel.t("mealsServed").uppercase(),
                value = "${attendance.size}",
                detail = viewModel.t("vsLastWeek"),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = viewModel.t("revenue").uppercase(),
                value = "₹${"%.1f".format(revenue / 1000.0)}k",
                detail = viewModel.t("thisMonth"),
                tone = BadgeTone.AMBER,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = viewModel.t("expenses").uppercase(),
                value = "₹${"%.1f".format(spend / 1000.0)}k",
                detail = viewModel.t("thisMonth"),
                tone = BadgeTone.RED,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = viewModel.t("activeMembers").uppercase(),
                value = "${customers.size}",
                detail = viewModel.t("attendanceRate"),
                modifier = Modifier.weight(1f)
            )
        }

        SectionHeading(title = viewModel.t("runMessAction"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconTile(Icons.Default.People, viewModel.t("customers"), onClick = { navController.navigate(Routes.CUSTOMERS) }, modifier = Modifier.weight(1f))
            IconTile(Icons.Default.RestaurantMenu, viewModel.t("menuPlan"), onClick = { navController.navigate(Routes.MENU) }, modifier = Modifier.weight(1f))
            IconTile(Icons.Default.DateRange, viewModel.t("leaves"), onClick = { navController.navigate(Routes.LEAVE) }, modifier = Modifier.weight(1f))
            IconTile(Icons.Default.Inventory, viewModel.t("inventory"), onClick = { navController.navigate(Routes.INVENTORY) }, modifier = Modifier.weight(1f))
        }

        PrimaryButton(label = viewModel.t("openAdminPanel"), icon = Icons.Default.GridOn, onClick = { navController.navigate(Routes.ADMIN) })

        SectionHeading(title = viewModel.t("needsAttention"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            // Attention items logic...
            Text("Needs attention content here...", modifier = Modifier.padding(14.dp), color = colors.mutedForeground)
        }
    }
}

// Temporary calculation logic port for UI
data class ExpiryInfo(val customer: com.bhojnify.core.model.Customer, val days: Long, val isExpiringSoon: Boolean, val isExpired: Boolean)

private fun reactToCustomers(customers: List<com.bhojnify.core.model.Customer>): List<ExpiryInfo> {
    val today = LocalDate.now().toString()
    return customers.map {
        val days = DateUtils.daysBetween(today, it.expiryDate)
        ExpiryInfo(it, days, days in 0..3, days < 0)
    }.filter { it.isExpiringSoon || it.isExpired }
}
