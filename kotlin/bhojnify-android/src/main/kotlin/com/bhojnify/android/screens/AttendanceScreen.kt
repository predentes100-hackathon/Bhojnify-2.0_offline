package com.bhojnify.android.screens

import android.widget.Toast
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import com.bhojnify.core.model.Meal
import com.bhojnify.core.service.DateUtils

@Composable
fun AttendanceScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val today = DateUtils.todayString()
    val customers = state.customers
    val attendance = state.attendance

    // Active customers only (non-expired by date)
    val activeCustomers = customers.filter {
        val info = DateUtils.getExpiryInfo(it.expiryDate)
        !info.isExpired
    }

    // Meal selector state
    var selectedMeal by remember { mutableStateOf(Meal.LUNCH) }

    // Set of customer IDs already marked for selected meal today
    val markedToday by remember(attendance, selectedMeal) {
        derivedStateOf {
            attendance
                .filter { it.date == today && it.meal == selectedMeal }
                .mapNotNull { it.customerId }
                .toSet()
        }
    }

    // Selection state: customers the owner wants to mark in this session
    var selected by remember(selectedMeal) { mutableStateOf(markedToday.toMutableSet()) }
    LaunchedEffect(markedToday) {
        selected = markedToday.toMutableSet()
    }

    // Summary numbers
    val todayTotal = attendance.count { it.date == today }
    val totalPerMeal = attendance.count { it.date == today && it.meal == selectedMeal }
    val attendanceRate = if (activeCustomers.isNotEmpty())
        (totalPerMeal * 100) / activeCustomers.size else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header(
            eyebrow = "Daily Tracking · $today",
            title = viewModel.t("attendance"),
            subtitle = viewModel.t("markAttendanceSubtitle")
        )

        // Today summary cards
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = viewModel.t("today").uppercase(),
                value = "$todayTotal",
                detail = viewModel.t("mealsServed"),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = viewModel.t("rate").uppercase(),
                value = "$attendanceRate%",
                detail = "${selectedMeal.displayName} attendance",
                tone = when {
                    attendanceRate >= 80 -> BadgeTone.GREEN
                    attendanceRate >= 50 -> BadgeTone.AMBER
                    else -> BadgeTone.RED
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Meal selector
        SectionHeading(title = viewModel.t("selectMeal"))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .background(colors.card)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Meal.entries.forEach { meal ->
                val isSelected = selectedMeal == meal
                val mealIcon = when (meal) {
                    Meal.BREAKFAST -> Icons.Default.WbSunny
                    Meal.LUNCH -> Icons.Default.Restaurant
                    Meal.DINNER -> Icons.Default.Bedtime
                }
                val alreadyCount = attendance.count { it.date == today && it.meal == meal }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.primary else Color.Transparent)
                        .clickable { selectedMeal = meal }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(
                            mealIcon, null,
                            tint = if (isSelected) colors.primaryForeground else colors.mutedForeground,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = meal.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) colors.primaryForeground else colors.foreground
                        )
                        if (alreadyCount > 0) {
                            Text(
                                text = "$alreadyCount marked",
                                fontSize = 10.sp,
                                color = if (isSelected) colors.primaryForeground.copy(alpha = 0.8f) else colors.mutedForeground
                            )
                        }
                    }
                }
            }
        }

        // Mark All / Clear All
        SectionHeading(
            title = "${selectedMeal.displayName} — ${activeCustomers.size} active members",
            action = if (selected.size == activeCustomers.size) "Clear All" else "Mark All",
            onActionClick = {
                if (selected.size == activeCustomers.size) {
                    selected = mutableSetOf()
                } else {
                    selected = activeCustomers.map { it.id }.toMutableSet()
                }
            }
        )

        // Customer list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            if (activeCustomers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.People,
                    title = viewModel.t("noActiveCustomers"),
                    detail = viewModel.t("noActiveCustomersDetail")
                )
            } else {
                activeCustomers.forEach { customer ->
                    val isMarked = customer.id in selected
                    val alreadySaved = customer.id in markedToday

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (isMarked) {
                                    (selected - customer.id).toMutableSet()
                                } else {
                                    (selected + customer.id).toMutableSet()
                                }
                            }
                            .padding(vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar / check indicator
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(if (isMarked) colors.primary else colors.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isMarked) {
                                Icon(Icons.Default.Check, null, tint = colors.primaryForeground, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    text = customer.name.take(2).uppercase(),
                                    color = colors.foreground,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = customer.name,
                                color = colors.foreground,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = customer.plan,
                                color = colors.mutedForeground,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (alreadySaved) {
                            Badge(label = viewModel.t("saved"), tone = BadgeTone.GREEN)
                        }
                    }
                }
            }
        }

        // Save button
        if (activeCustomers.isNotEmpty()) {
            PrimaryButton(
                label = "Save ${selectedMeal.displayName} Attendance (${selected.size})",
                icon = Icons.Default.Save,
                onClick = {
                    val toAdd = selected.toList()
                    val toRemove = (markedToday - selected.toSet()).toList()

                    // Mark newly selected
                    if (toAdd.isNotEmpty()) {
                        viewModel.markAttendanceBatch(toAdd, selectedMeal)
                    }
                    // Unmark deselected
                    toRemove.forEach { customerId ->
                        viewModel.unmarkAttendance(customerId, selectedMeal)
                    }

                    val msg = when {
                        toAdd.isEmpty() && toRemove.isEmpty() -> "No changes"
                        toAdd.isNotEmpty() && toRemove.isNotEmpty() ->
                            "${toAdd.size} marked, ${toRemove.size} unmarked"
                        toAdd.isNotEmpty() -> "${toAdd.size} marked for ${selectedMeal.displayName}"
                        else -> "${toRemove.size} unmarked"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Today's full log
        val todayLog = attendance.filter { it.date == today }
        if (todayLog.isNotEmpty()) {
            SectionHeading(title = viewModel.t("todaysLog"))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(19.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                    .background(colors.card)
                    .padding(horizontal = 14.dp)
            ) {
                Meal.entries.forEach { meal ->
                    val mealRecords = todayLog.filter { it.meal == meal }
                    if (mealRecords.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                val mealIcon = when (meal) {
                                    Meal.BREAKFAST -> Icons.Default.WbSunny
                                    Meal.LUNCH -> Icons.Default.Restaurant
                                    Meal.DINNER -> Icons.Default.Bedtime
                                }
                                Icon(mealIcon, null, tint = colors.primary, modifier = Modifier.size(17.dp))
                            }
                            Text(
                                text = meal.displayName,
                                color = colors.foreground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${mealRecords.size} / ${activeCustomers.size}",
                                color = colors.mutedForeground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Badge(
                                label = "${if (activeCustomers.isNotEmpty()) (mealRecords.size * 100) / activeCustomers.size else 0}%",
                                tone = when {
                                    activeCustomers.isNotEmpty() && (mealRecords.size * 100) / activeCustomers.size >= 80 -> BadgeTone.GREEN
                                    activeCustomers.isNotEmpty() && (mealRecords.size * 100) / activeCustomers.size >= 50 -> BadgeTone.AMBER
                                    else -> BadgeTone.RED
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
