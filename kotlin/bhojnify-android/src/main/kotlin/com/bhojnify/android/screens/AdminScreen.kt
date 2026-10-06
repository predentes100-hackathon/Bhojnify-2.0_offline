package com.bhojnify.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.bhojnify.core.model.LeaveStatus

@Composable
fun AdminScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()

    val profile = state.profile
    val inventory = state.inventory
    val attendance = state.attendance
    val customers = state.customers
    val leaves = state.leaves
    val payments = state.payments
    val expenses = state.expenses
    val feedback = state.feedback

    val pendingLeaves = leaves.filter { it.status == LeaveStatus.PENDING }
    val lowStock = inventory.filter { it.quantity <= it.minimum }
    val revenue = payments.sumOf { it.amount }
    val spend = expenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Header(
            eyebrow = viewModel.t("controlRoom"),
            title = viewModel.t("operationsPanel"),
            subtitle = viewModel.t("importantThings"),
            onActionClick = { navController.navigate(Routes.PROFILE) }
        )

        // Hero Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(23.dp))
                .background(colors.foreground)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = (profile.messName.ifEmpty { viewModel.t("yourMess") }).uppercase(),
                    color = colors.accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
                Text(
                    text = viewModel.t("calmerKitchen"),
                    color = colors.card,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.t("monitorService"),
                    color = colors.card.copy(alpha = 0.76f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            Icon(Icons.Default.Dashboard, null, tint = colors.accent, modifier = Modifier.size(42.dp))
        }

        // Stats Row 1
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = viewModel.t("attendance").uppercase(),
                value = "${attendance.size}",
                detail = viewModel.t("todayRecords"),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = viewModel.t("pending").uppercase(),
                value = "${pendingLeaves.size}",
                detail = viewModel.t("leaveApprovals"),
                tone = BadgeTone.AMBER,
                modifier = Modifier.weight(1f)
            )
        }

        // Stats Row 2
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = viewModel.t("lowStock").uppercase(),
                value = "${lowStock.size}",
                detail = viewModel.t("itemsToReorder"),
                tone = BadgeTone.RED,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = viewModel.t("surplus").uppercase(),
                value = "₹${"%.1f".format((revenue - spend) / 1000.0)}k",
                detail = viewModel.t("thisMonth"),
                modifier = Modifier.weight(1f)
            )
        }

        SectionHeading(title = viewModel.t("manageOperations"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconTile(Icons.Default.Inventory, viewModel.t("inventory"), onClick = { navController.navigate(Routes.INVENTORY) }, modifier = Modifier.weight(1f))
            IconTile(Icons.Default.RestaurantMenu, viewModel.t("menu"), onClick = { navController.navigate(Routes.MENU) }, modifier = Modifier.weight(1f))
            IconTile(Icons.Default.People, viewModel.t("staff"), onClick = { navController.navigate(Routes.STAFF) }, modifier = Modifier.weight(1f))
            IconTile(Icons.Default.MoneyOff, viewModel.t("expenses"), onClick = { navController.navigate(Routes.EXPENSES) }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconTile(Icons.Default.Groups, viewModel.t("customers"), onClick = { navController.navigate(Routes.CUSTOMERS) }, modifier = Modifier.weight(1f))
            IconTile(Icons.Default.BarChart, viewModel.t("reports"), onClick = { navController.navigate(Routes.REPORT) }, modifier = Modifier.weight(1f))
            IconTile(Icons.Default.Payment, viewModel.t("payments"), onClick = { navController.navigate(Routes.PAYMENTS) }, modifier = Modifier.weight(1f))
            IconTile(Icons.Default.DateRange, viewModel.t("leaveLog"), onClick = { navController.navigate(Routes.LEAVE) }, modifier = Modifier.weight(1f))
        }

        SectionHeading(
            title = viewModel.t("leaveApprovals"),
            action = if (pendingLeaves.isNotEmpty()) viewModel.t("waiting", mapOf("count" to pendingLeaves.size)) else null
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            if (pendingLeaves.isNotEmpty()) {
                pendingLeaves.take(3).forEach { leave ->
                    val customer = customers.find { it.id == leave.customerId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = colors.primary, modifier = Modifier.size(17.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customer?.name ?: viewModel.t("memberLeave"),
                                color = colors.foreground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${leave.from} – ${leave.to ?: viewModel.t("returnPending")}${if (leave.reason.isNotEmpty()) " · ${leave.reason}" else ""}",
                                color = colors.mutedForeground,
                                fontSize = 11.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(colors.secondary)
                                    .clickable { viewModel.updateLeaveStatus(leave.id, LeaveStatus.APPROVED) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = colors.primary, modifier = Modifier.size(17.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(colors.muted)
                                    .clickable { viewModel.updateLeaveStatus(leave.id, LeaveStatus.DECLINED) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, null, tint = colors.destructive, modifier = Modifier.size(17.dp))
                            }
                        }
                    }
                }
            } else {
                RowItem(
                    icon = Icons.Default.CheckCircle,
                    title = viewModel.t("nothingWaiting"),
                    detail = viewModel.t("caughtUpLeave"),
                    rightContent = { Badge(label = viewModel.t("clear")) }
                )
            }
        }

        SectionHeading(
            title = viewModel.t("latestFeedback"),
            action = viewModel.t("viewAll"),
            onActionClick = { navController.navigate(Routes.FEEDBACK) }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            if (feedback.isNotEmpty()) {
                feedback.take(2).forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(item.rating) {
                                Icon(Icons.Default.Star, null, tint = colors.accent, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = item.note.ifEmpty { viewModel.t("rated", mapOf("dish" to item.dish)) },
                            color = colors.foreground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.dish,
                            color = colors.mutedForeground,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Text(
                    text = viewModel.t("noFeedback"),
                    color = colors.mutedForeground,
                    fontSize = 13.sp
                )
            }
        }
    }
}