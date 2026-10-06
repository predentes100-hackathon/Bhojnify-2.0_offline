package com.bhojnify.android.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel

@Composable
fun ExpensesScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val expenses = state.expenses
    var category by remember { mutableStateOf("Inventory") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val totalSpend = expenses.sumOf { it.amount }

    val add = {
        val a = amount.toDoubleOrNull()
        if (a == null || note.trim().isEmpty()) {
            Toast.makeText(context, viewModel.t("expenseRequiredMessage"), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addExpense(category.trim(), a, note.trim())
            amount = ""
            note = ""
            Toast.makeText(context, "Expense saved!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Header(
            eyebrow = viewModel.t("ownerTools"),
            title = viewModel.t("expensesTitle"),
            subtitle = viewModel.t("expensesSubtitle"),
            onActionClick = { navController.navigate(Routes.PROFILE) }
        )

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

        SectionHeading(title = viewModel.t("recentExpenses"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            if (expenses.isNotEmpty()) {
                expenses.forEach { expense ->
                    val icon = when (expense.category) {
                        "Gas" -> Icons.Default.LocalFireDepartment
                        "Maintenance" -> Icons.Default.Build
                        else -> Icons.Default.ShoppingCart
                    }

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
                            Icon(icon, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(text = viewModel.localized(expense.category), color = colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = expense.note, color = colors.mutedForeground, fontSize = 11.sp)
                        }
                        Text(text = "₹${"%,.0f".format(expense.amount)}", color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                EmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Expenses Logged",
                    detail = "Record grocery, gas, maintenance, and kitchen utility expenses."
                )
            }
        }

        SectionHeading(title = viewModel.t("logExpense"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            FormField(label = viewModel.t("category"), value = category, onValueChange = { category = it }, placeholder = viewModel.t("enterExpenseCategory"))
            FormField(label = viewModel.t("amount"), value = amount, onValueChange = { amount = it }, placeholder = viewModel.t("enterExpenseAmount"), keyboardType = KeyboardType.Number)
            FormField(label = viewModel.t("note"), value = note, onValueChange = { note = it }, placeholder = viewModel.t("enterExpenseNote"))

            PrimaryButton(label = viewModel.t("saveExpense"), icon = Icons.Default.Add, onClick = add)
        }
    }
}