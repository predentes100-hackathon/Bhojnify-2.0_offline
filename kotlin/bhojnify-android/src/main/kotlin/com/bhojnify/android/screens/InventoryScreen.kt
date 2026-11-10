package com.bhojnify.android.screens

import android.widget.Toast
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
import java.time.LocalDate

@Composable
fun InventoryScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val inventory = state.inventory
    val reminders = state.reminders
    val currentDate = LocalDate.now().toString()

    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var minimum by remember { mutableStateOf("") }
    var reminderTitle by remember { mutableStateOf("") }
    var reminderDays by remember { mutableStateOf("") }

    val add = {
        val q = quantity.toDoubleOrNull()
        val m = minimum.toDoubleOrNull()
        val d = reminderDays.toLongOrNull()

        if (name.trim().isEmpty() || q == null || m == null) {
            Toast.makeText(context, viewModel.t("itemDetailsMessage"), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addInventory(name.trim(), q, unit.trim().ifEmpty { "kg" }, m)
            if (reminderTitle.trim().isNotEmpty() && d != null) {
                viewModel.addReminder(reminderTitle.trim(), d, "${viewModel.t("inventory")}: ${name.trim()}")
            }
            name = ""
            quantity = ""
            minimum = ""
            reminderTitle = ""
            reminderDays = ""
            Toast.makeText(context, viewModel.t("inventoryItemAdded"), Toast.LENGTH_SHORT).show()
        }
    }

    val lowStockCount = inventory.count { it.quantity <= it.minimum }

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
            title = viewModel.t("inventoryLedger"),
            subtitle = viewModel.t("stayAhead"),
            onActionClick = { navController.navigate(Routes.PROFILE) }
        )

        if (lowStockCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .background(colors.accent)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(Icons.Default.Warning, null, tint = colors.accentForeground, modifier = Modifier.size(20.dp))
                Text(
                    text = viewModel.t("itemBelowMinimum", mapOf("count" to lowStockCount)),
                    color = colors.accentForeground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        SectionHeading(title = viewModel.t("currentStock"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            if (inventory.isNotEmpty()) {
                inventory.forEach { item ->
                    val low = item.quantity <= item.minimum
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
                                .background(if (low) colors.accent else colors.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                null,
                                tint = if (low) colors.accentForeground else colors.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(text = item.name, color = colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${viewModel.localized(item.category)} · ${viewModel.t("minimum")} ${item.minimum} ${item.unit}",
                                color = colors.mutedForeground,
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = "${item.quantity} ${item.unit}",
                                color = if (low) colors.destructive else colors.foreground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(label = if (low) viewModel.t("low") else viewModel.t("good"), tone = if (low) BadgeTone.AMBER else BadgeTone.GREEN)
                        }
                        IconButton(onClick = { viewModel.removeInventory(item.id) }) {
                            Icon(Icons.Default.Delete, null, tint = colors.mutedForeground, modifier = Modifier.size(17.dp))
                        }
                    }
                }
            } else {
                EmptyState(
                    icon = Icons.Default.Inventory,
                    title = viewModel.t("noInventoryItems"),
                    detail = viewModel.t("noInventoryItemsDetail")
                )
            }
        }

        SectionHeading(title = viewModel.t("activeReminders"), action = "${reminders.size}")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            if (reminders.isNotEmpty()) {
                reminders.forEach { reminder ->
                    val due = reminder.dueDate <= currentDate
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
                                .background(if (due) colors.accent else colors.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                null,
                                tint = if (due) colors.accentForeground else colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(text = reminder.title, color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${reminder.detail} · ${if (due) viewModel.t("dueNow") else reminder.dueDate}",
                                color = colors.mutedForeground,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (due) colors.accent else colors.secondary)
                                .clickable { viewModel.resolveReminder(reminder.id) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = viewModel.t("resolve"),
                                color = if (due) colors.accentForeground else colors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = viewModel.t("noActiveReminders"),
                    color = colors.mutedForeground,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        SectionHeading(title = viewModel.t("addStockItem"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            FormField(label = viewModel.t("ingredient"), value = name, onValueChange = { name = it }, placeholder = "e.g. Rice, Dal, Milk")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormField(
                    label = viewModel.t("quantity"),
                    value = quantity,
                    onValueChange = { quantity = it },
                    placeholder = "e.g. 50",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                FormField(
                    label = viewModel.t("minimum"),
                    value = minimum,
                    onValueChange = { minimum = it },
                    placeholder = "e.g. 10",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
            FormField(label = viewModel.t("unit"), value = unit, onValueChange = { unit = it }, placeholder = "e.g. kg, L, Packets")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .background(colors.secondary)
                    .padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Default.Notifications, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Text(text = viewModel.t("addFollowUp"), color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = viewModel.t("reminderExplanation"), color = colors.secondaryForeground, fontSize = 11.sp, lineHeight = 16.sp)
                FormField(label = viewModel.t("reminderTitle"), value = reminderTitle, onValueChange = { reminderTitle = it }, placeholder = "e.g. Recheck Rice Stock")
                FormField(label = viewModel.t("showAfterDays"), value = reminderDays, onValueChange = { reminderDays = it }, placeholder = "e.g. 3", keyboardType = KeyboardType.Number)
            }

            PrimaryButton(label = viewModel.t("addToLedger"), icon = Icons.Default.Add, onClick = add)
        }
    }
}