package com.bhojnify.android.screens

import com.bhojnify.core.model.Expense

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import kotlin.math.cos
import kotlin.math.sin

// ── Default categories ───────────────────────────────────────────────────────
private val DEFAULT_CATEGORIES = listOf(
    "Inventory",
    "Grocery",
    "Gas",
    "Electricity",
    "Water",
    "Maintenance",
    "Salary",
    "Rent",
    "Packaging",
    "Other"
)

// ── Chart palette — 10 distinct hues, colorblind-friendly order ───────────────
private val SLICE_COLORS = listOf(
    Color(0xFF4C6EF5), // indigo
    Color(0xFFF76707), // orange
    Color(0xFF2F9E44), // green
    Color(0xFFE03131), // red
    Color(0xFF0CA678), // teal
    Color(0xFFAE3EC9), // purple
    Color(0xFF1971C2), // blue
    Color(0xFFE67700), // amber
    Color(0xFF099268), // emerald
    Color(0xFFC2255C), // pink
)

private fun colorForIndex(i: Int): Color = SLICE_COLORS[i % SLICE_COLORS.size]

// ── Pie chart drawn entirely with Compose Canvas ─────────────────────────────
@androidx.compose.runtime.Composable
private fun ExpensePieChart(
    slices: List<Pair<String, Double>>,      // label → amount
    total: Double,
    breakdownTitle: String = "Breakdown",
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty() || total == 0.0) return

    val colors = BhojnifyTheme.colors
    val sweeps = slices.map { (it.second / total * 360f).toFloat() }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // ── Donut ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            val strokeWidth = 52.dp

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(200.dp)
            ) {
                val diameter = size.minDimension
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                val gapSweep = 1.5f          // 1.5° white gap between slices

                var startAngle = -90f
                sweeps.forEachIndexed { i, sweep ->
                    // filled arc
                    drawArc(
                        color = colorForIndex(i),
                        startAngle = startAngle + gapSweep / 2f,
                        sweepAngle = sweep - gapSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth.toPx())
                    )
                    startAngle += sweep
                }

                // surface-color ring to create donut hole
                drawCircle(
                    color = Color(0xFF1A1A2E).copy(alpha = 0f), // transparent — donut formed by stroke style
                    radius = 0f
                )
            }

            // Center label: total
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "₹${"%,.0f".format(total)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.foreground
                )
                Text(
                    text = "Total",
                    fontSize = 11.sp,
                    color = colors.mutedForeground,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Legend (category · color swatch · amount · %) ─────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.card)
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(breakdownTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)

            slices.forEachIndexed { i, (label, amount) ->
                val pct = if (total > 0) amount / total * 100 else 0.0
                val barFrac = if (total > 0) (amount / total).toFloat() else 0f

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // color swatch
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colorForIndex(i))
                        )
                        // label
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.foreground,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // percentage badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colorForIndex(i).copy(alpha = 0.15f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${"%.1f".format(pct)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorForIndex(i)
                            )
                        }
                        // amount
                        Text(
                            text = "₹${"%,.0f".format(amount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                    }

                    // thin progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barFrac)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(colorForIndex(i))
                        )
                    }
                }
            }
        }
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────
@Composable
fun ExpensesScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val expenses = state.expenses

    // Category state — combine defaults with any custom ones already saved
    val persistedCategories = state.expenseCategories.ifEmpty { DEFAULT_CATEGORIES }
    var customCategories by remember(persistedCategories) {
        mutableStateOf(persistedCategories.filter { it !in DEFAULT_CATEGORIES })
    }
    val allCategories = remember(customCategories) { DEFAULT_CATEGORIES + customCategories }

    var selectedCategory by remember { mutableStateOf(DEFAULT_CATEGORIES.first()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // Add-custom-category dialog state
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    val totalSpend = expenses.sumOf { it.amount }

    // Group by category, sorted by total descending for chart
    val categoryTotals: List<Pair<String, Double>> = remember(expenses) {
        expenses
            .groupBy { it.category }
            .map { (cat, list) -> Pair(cat, list.sumOf { it.amount }) }
            .sortedByDescending { it.second }
    }

    var attemptedSubmit by remember { mutableStateOf(false) }
    val amountVal = amount.toDoubleOrNull()
    val isAmountError = attemptedSubmit && (amountVal == null || amountVal <= 0)
    val isNoteError = attemptedSubmit && note.trim().isEmpty()

    val add = {
        attemptedSubmit = true
        val a = amount.toDoubleOrNull()
        if (a == null || a <= 0) {
            Toast.makeText(context, viewModel.t("enterValidAmount"), Toast.LENGTH_SHORT).show()
        } else if (note.trim().isEmpty()) {
            Toast.makeText(context, viewModel.t("expenseRequiredMessage"), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addExpense(selectedCategory.trim(), a, note.trim())
            amount = ""
            note = ""
            attemptedSubmit = false
            Toast.makeText(context, viewModel.t("expenseSaved"), Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "header") {
            Header(
                eyebrow = viewModel.t("ownerTools"),
                title = viewModel.t("expensesTitle"),
                subtitle = viewModel.t("expensesSubtitle"),
                actionContentDescription = viewModel.t("profile"),
                onActionClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        item(key = "hero_card") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(21.dp))
                    .background(colors.foreground)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = viewModel.t("septemberSpend").uppercase(),
                        color = colors.card.copy(alpha = 0.68f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Text(
                        text = "₹${"%,.0f".format(totalSpend)}",
                        color = colors.card,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
                Icon(Icons.Default.TrendingDown, null, tint = colors.accent, modifier = Modifier.size(30.dp))
            }
        }

        if (expenses.isNotEmpty()) {
            item(key = "pie_chart_heading") {
                SectionHeading(title = viewModel.t("spendingBreakdown"))
            }
            item(key = "pie_chart") {
                ExpensePieChart(
                    slices = categoryTotals,
                    total = totalSpend,
                    breakdownTitle = viewModel.t("breakdown"),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item(key = "recent_expenses_heading") {
            SectionHeading(title = viewModel.t("recentExpenses"))
        }

        if (expenses.isEmpty()) {
            item(key = "empty_state") {
                EmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = viewModel.t("noExpensesLogged"),
                    detail = viewModel.t("noExpensesLoggedDetail")
                )
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                val sliceIndex = categoryTotals.indexOfFirst { it.first == expense.category }
                val dotColor = if (sliceIndex >= 0) colorForIndex(sliceIndex) else colors.primary
                ExpenseRow(expense = expense, dotColor = dotColor)
            }
        }

        item(key = "log_expense_heading") {
            SectionHeading(title = viewModel.t("logExpense"))
        }

        item(key = "log_expense_card") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Category heading + Add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(viewModel.t("category"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.secondary)
                        .clickable { showAddCategoryDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Add, null, tint = colors.primary, modifier = Modifier.size(14.dp))
                        Text(viewModel.t("newCategory"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    }
                }
            }

            // Category chips — wrap using FlowRow-style layout via a looped Row
            val chunked = allCategories.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunked.forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) colors.primary else colors.border,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(if (isSelected) colors.primary else colors.background)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.primaryForeground else colors.foreground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            FormField(
                label = viewModel.t("amount"),
                value = amount,
                onValueChange = { amount = it },
                placeholder = viewModel.t("enterExpenseAmount"),
                keyboardType = KeyboardType.Number,
                isError = isAmountError,
                errorMessage = viewModel.t("enterValidAmount")
            )
            FormField(
                label = viewModel.t("note"),
                value = note,
                onValueChange = { note = it },
                placeholder = viewModel.t("enterExpenseNote"),
                isError = isNoteError,
                errorMessage = viewModel.t("fieldRequired")
            )

            PrimaryButton(label = viewModel.t("saveExpense"), icon = Icons.Default.Add, onClick = add)
        }
    }
}

    // ── Add-custom-category dialog ────────────────────────────────────
    if (showAddCategoryDialog) {
        Dialog(onDismissRequest = { showAddCategoryDialog = false; newCategoryInput = "" }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.card,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(viewModel.t("addCategory"), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                        IconButton(onClick = { showAddCategoryDialog = false; newCategoryInput = "" }) {
                            Icon(Icons.Default.Close, "Close", tint = colors.mutedForeground)
                        }
                    }

                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = { newCategoryInput = it },
                        label = { Text(viewModel.t("categoryName")) },
                        placeholder = { Text(viewModel.t("enterCategoryName")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(13.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedContainerColor = colors.background,
                            unfocusedContainerColor = colors.background
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAddCategoryDialog = false; newCategoryInput = "" },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.foreground),
                            shape = RoundedCornerShape(13.dp)
                        ) { Text(viewModel.t("cancel"), fontWeight = FontWeight.Bold) }

                        Button(
                            onClick = {
                                val trimmed = newCategoryInput.trim()
                                when {
                                    trimmed.isEmpty() ->
                                        Toast.makeText(context, viewModel.t("enterCategoryName"), Toast.LENGTH_SHORT).show()
                                    trimmed in allCategories ->
                                        Toast.makeText(context, viewModel.t("categoryAlreadyExists"), Toast.LENGTH_SHORT).show()
                                    else -> {
                                        customCategories = customCategories + trimmed
                                        selectedCategory = trimmed
                                        newCategoryInput = ""
                                        showAddCategoryDialog = false
                                        Toast.makeText(context, viewModel.t("categoryAdded", mapOf("name" to trimmed)), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(2f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.primaryForeground),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.t("add"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseRow(
    expense: Expense,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = BhojnifyTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .background(colors.card)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(dotColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (expense.category) {
                "Gas" -> Icons.Default.LocalFireDepartment
                "Maintenance" -> Icons.Default.Build
                "Electricity" -> Icons.Default.ElectricBolt
                "Water" -> Icons.Default.WaterDrop
                "Grocery" -> Icons.Default.ShoppingBasket
                "Salary" -> Icons.Default.People
                "Rent" -> Icons.Default.Home
                "Packaging" -> Icons.Default.Inventory2
                else -> Icons.Default.ShoppingCart
            }
            Icon(icon, null, tint = dotColor, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = expense.category, color = colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = expense.note, color = colors.mutedForeground, fontSize = 11.sp)
            Text(text = expense.date, color = colors.mutedForeground, fontSize = 10.sp)
        }
        Text(
            text = "₹${"%,.0f".format(expense.amount)}",
            color = colors.foreground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
