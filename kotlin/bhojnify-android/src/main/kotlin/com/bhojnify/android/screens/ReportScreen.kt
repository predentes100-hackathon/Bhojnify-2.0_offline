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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import com.bhojnify.core.service.DateUtils
import kotlin.math.abs

@Composable
fun ReportScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val summary by viewModel.reportSummary.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedReportMonth.collectAsStateWithLifecycle()
    val growth by viewModel.monthlyRevenueGrowth.collectAsStateWithLifecycle()
    val expenseBreakdown by viewModel.selectedMonthExpenseBreakdown.collectAsStateWithLifecycle()
    val paymentBreakdown by viewModel.selectedMonthPaymentBreakdown.collectAsStateWithLifecycle()

    val currentMonth = remember { DateUtils.currentYearMonth() }
    val isCurrentMonth = selectedMonth == currentMonth
    val monthTitle = DateUtils.formatYearMonth(selectedMonth, state.language)

    val hasAnyData = summary.totalRevenue > 0.0 || summary.totalExpenses > 0.0 || summary.mealsServedCount > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header(
            eyebrow = viewModel.t("ownerTools"),
            title = "${viewModel.t("monthlyReport")} · $monthTitle",
            subtitle = viewModel.t("reportSubtitle"),
            onActionClick = { navController.navigate(Routes.PROFILE) }
        )

        // Month Selector Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.card)
                .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.selectPreviousReportMonth() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = viewModel.t("previousMonth"),
                        tint = colors.foreground,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable {
                        if (!isCurrentMonth) viewModel.selectCurrentReportMonth()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = monthTitle,
                        color = colors.foreground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isCurrentMonth) {
                        Badge(label = viewModel.t("currentMonth"), tone = BadgeTone.GREEN)
                    } else {
                        Badge(label = viewModel.t("currentMonth"), tone = BadgeTone.GRAY)
                    }
                }

                IconButton(
                    onClick = { viewModel.selectNextReportMonth() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = viewModel.t("nextMonth"),
                        tint = colors.foreground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Operating Surplus / Deficit Hero Card
        val isSurplus = summary.operatingSurplus >= 0.0
        val heroBg = if (isSurplus) colors.primary else colors.foreground
        val heroIcon = if (isSurplus) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
        val heroLabel = if (isSurplus) viewModel.t("operatingSurplus") else viewModel.t("operatingDeficit")
        val heroSubtitle = if (isSurplus) viewModel.t("revenueLessExpenses") else viewModel.t("operatingCostsExceedRevenue")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(heroBg)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = heroLabel.uppercase(),
                    color = colors.primaryForeground.copy(alpha = 0.72f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
                Text(
                    text = (if (isSurplus) "₹" else "-₹") + "%,.0f".format(abs(summary.operatingSurplus)),
                    color = colors.primaryForeground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = heroSubtitle,
                    color = colors.primaryForeground.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = heroIcon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // 4-Card Performance Grid
        // Row 1: Revenue & Expenses
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .padding(15.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = viewModel.t("revenue").uppercase(),
                        color = colors.mutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "₹${"%,.0f".format(summary.totalRevenue)}",
                        color = colors.foreground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val g = growth
                    when {
                        g == null -> Badge(label = viewModel.t("firstMonth"), tone = BadgeTone.AMBER)
                        g >= 0.0 -> Badge(label = "+${"%.1f".format(g)}%", tone = BadgeTone.GREEN)
                        else -> Badge(label = "${"%.1f".format(g)}%", tone = BadgeTone.RED)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .padding(15.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = viewModel.t("expenses").uppercase(),
                        color = colors.mutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "₹${"%,.0f".format(summary.totalExpenses)}",
                        color = colors.foreground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Badge(label = viewModel.t("tracked"), tone = BadgeTone.GRAY)
                }
            }
        }

        // Row 2: Meals Served & Stock Valuation
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .padding(15.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = viewModel.t("mealsServed").uppercase(),
                        color = colors.mutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "${summary.mealsServedCount}",
                        color = colors.foreground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Badge(label = monthTitle, tone = BadgeTone.GRAY)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .padding(15.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = viewModel.t("stockValueEstimate").uppercase(),
                        color = colors.mutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "₹${"%,.0f".format(summary.stockValueEstimate)}",
                        color = colors.foreground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Badge(label = viewModel.t("currentStock"), tone = BadgeTone.AMBER)
                }
            }
        }

        // Spending Breakdown Section
        if (expenseBreakdown.isNotEmpty()) {
            SectionHeading(title = viewModel.t("spendingBreakdown"))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val totalExp = if (summary.totalExpenses > 0.0) summary.totalExpenses else 1.0
                expenseBreakdown.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                    val pct = ((amt / totalExp) * 100).toInt()
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = viewModel.localized(cat),
                                color = colors.foreground,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "₹${"%,.0f".format(amt)} ($pct%)",
                                color = colors.mutedForeground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (amt / totalExp).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.primary,
                            trackColor = colors.muted
                        )
                    }
                }
            }
        }

        // Payment Method Breakdown Section
        if (paymentBreakdown.isNotEmpty()) {
            SectionHeading(title = viewModel.t("paymentMethodBreakdown"))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val totalRev = if (summary.totalRevenue > 0.0) summary.totalRevenue else 1.0
                paymentBreakdown.entries.sortedByDescending { it.value }.forEach { (method, amt) ->
                    val pct = ((amt / totalRev) * 100).toInt()
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = viewModel.localized(method),
                                color = colors.foreground,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "₹${"%,.0f".format(amt)} ($pct%)",
                                color = colors.mutedForeground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (amt / totalRev).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.accent,
                            trackColor = colors.muted
                        )
                    }
                }
            }
        }

        // Empty state when there are no transactions in the selected month
        if (!hasAnyData) {
            EmptyState(
                icon = Icons.Default.EventBusy,
                title = viewModel.t("noDataForMonth"),
                detail = viewModel.t("noDataForMonthDetail")
            )
        }

        // Navigation Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PrimaryButton(
                label = viewModel.t("manageExpenses"),
                icon = Icons.Default.ReceiptLong,
                secondary = true,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.EXPENSES) }
            )
            PrimaryButton(
                label = viewModel.t("viewPaymentLedger"),
                icon = Icons.Default.Payment,
                secondary = true,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Routes.PAYMENTS) }
            )
        }
    }
}
