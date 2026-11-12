package com.bhojnify.android.screens

import com.bhojnify.core.model.Customer
import com.bhojnify.core.model.LeaveRequest
import com.bhojnify.core.model.LeaveStatus
import com.bhojnify.core.service.DateUtils

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import java.time.LocalDate

@Composable
fun LeaveScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val customers = state.customers
    val leaves = state.leaves
    val todayStr = LocalDate.now().toString()

    var showForm by remember { mutableStateOf(false) }
    var editingLeaveId by remember { mutableStateOf<String?>(null) }
    var customerId by remember { mutableStateOf(customers.firstOrNull()?.id ?: "") }
    var from by remember { mutableStateOf(todayStr) }
    var to by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var completingLeaveId by remember { mutableStateOf<String?>(null) }
    var returnDateInput by remember { mutableStateOf(todayStr) }

    val pendingLeaves = leaves.filter { it.status == LeaveStatus.PENDING || it.to == null }

    val resetForm = {
        customerId = customers.firstOrNull()?.id ?: ""
        from = todayStr
        to = ""
        note = ""
        editingLeaveId = null
        showForm = false
    }

    val saveLeave = {
        if (customerId.isEmpty() || from.trim().isEmpty()) {
            Toast.makeText(context, viewModel.t("leaveRequiredMessage"), Toast.LENGTH_SHORT).show()
        } else {
            val toDate = to.trim().ifEmpty { null }
            if (editingLeaveId != null) {
                viewModel.updateLeave(editingLeaveId!!, from.trim(), toDate, note.trim(), customerId)
            } else {
                viewModel.addLeave(customerId, from.trim(), toDate, note.trim())
            }
            resetForm()
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
                title = viewModel.t("leaveTitle"),
                subtitle = viewModel.t("leaveSubtitle"),
                actionContentDescription = viewModel.t("profile"),
                onActionClick = { navController.navigate(Routes.ADMIN) }
            )
        }

        item(key = "summary_card") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.secondary)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.DateRange, null, tint = colors.primary, modifier = Modifier.size(22.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = viewModel.t("requestWaiting", mapOf("count" to pendingLeaves.size, "plural" to if (pendingLeaves.size == 1) "" else "s")),
                        color = colors.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = viewModel.t("approvedLeaveDetail"),
                        color = colors.secondaryForeground,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item(key = "action_btn") {
            PrimaryButton(
                label = if (showForm) viewModel.t("cancel") else viewModel.t("addLeave"),
                icon = if (showForm) Icons.Default.Close else Icons.Default.Add,
                onClick = {
                    if (showForm) resetForm()
                    else {
                        resetForm()
                        showForm = true
                    }
                }
            )
        }

        // Complete Leave Confirmation
        if (completingLeaveId != null) {
            item(key = "completing_leave") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, colors.primary, RoundedCornerShape(18.dp))
                        .background(colors.card)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = colors.primary, modifier = Modifier.size(22.dp))
                        Text(text = viewModel.t("completeLeave"), color = colors.foreground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = viewModel.t("autoExtendSub"), color = colors.mutedForeground, fontSize = 12.sp, lineHeight = 17.sp)
                    FormField(label = viewModel.t("returnDate"), value = returnDateInput, onValueChange = { returnDateInput = it }, placeholder = "YYYY-MM-DD")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.align(Alignment.End)) {
                        OutlinedButton(onClick = { completingLeaveId = null }) {
                            Text(text = viewModel.t("cancel"))
                        }
                        Button(
                            onClick = {
                                viewModel.completeLeave(completingLeaveId!!, returnDateInput.trim())
                                completingLeaveId = null
                                Toast.makeText(context, viewModel.t("planExtendedNotice"), Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text(text = viewModel.t("confirmReturn"))
                        }
                    }
                }
            }
        }

        // Form Card
        if (showForm) {
            item(key = "form_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                        .background(colors.card)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text(
                        text = if (editingLeaveId != null) viewModel.t("editLeaveTitle") else viewModel.t("addLeaveTitle"),
                        color = colors.foreground,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(text = viewModel.t("selectCustomer"), color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        customers.forEach { customer ->
                            val isSelected = customerId == customer.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(13.dp))
                                    .background(if (isSelected) colors.secondary else colors.background)
                                    .clickable { customerId = customer.id }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(text = customer.name, color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "${customer.plan} · ${viewModel.localized(customer.paymentStatus.displayName)}", color = colors.mutedForeground, fontSize = 10.sp)
                                    }
                                    Icon(
                                        if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        null,
                                        tint = if (isSelected) colors.primary else colors.mutedForeground
                                    )
                                }
                            }
                        }
                    }

                    FormField(label = viewModel.t("leaveFrom"), value = from, onValueChange = { from = it }, placeholder = "YYYY-MM-DD")
                    FormField(label = viewModel.t("leaveToOptional"), value = to, onValueChange = { to = it }, placeholder = viewModel.t("leaveToPlaceholder"))
                    FormField(label = viewModel.t("leaveNoteOptional"), value = note, onValueChange = { note = it }, placeholder = viewModel.t("enterLeaveNote"))

                    PrimaryButton(
                        label = if (editingLeaveId != null) viewModel.t("saveChanges") else viewModel.t("saveLeave"),
                        icon = Icons.Default.Check,
                        onClick = saveLeave,
                        disabled = customers.isEmpty()
                    )
                }
            }
        }

        item(key = "requests_heading") {
            SectionHeading(title = viewModel.t("allRequests"))
        }

        if (leaves.isEmpty()) {
            item(key = "empty_state") {
                EmptyState(
                    icon = Icons.Default.DateRange,
                    title = viewModel.t("noLeaveRequests"),
                    detail = viewModel.t("caughtUpLeave")
                )
            }
        } else {
            items(leaves, key = { it.id }) { leave ->
                val customer = customers.find { it.id == leave.customerId }
                LeaveRow(
                    leave = leave,
                    customer = customer,
                    pendingLabel = viewModel.t("holidayPending"),
                    statusLabel = viewModel.localized(leave.status.displayName),
                    memberLeaveLabel = viewModel.t("memberLeave"),
                    completeLabel = viewModel.t("completeHoliday"),
                    onCompleteClick = {
                        completingLeaveId = leave.id
                        returnDateInput = DateUtils.todayString()
                    },
                    onEditClick = {
                        editingLeaveId = leave.id
                        customerId = leave.customerId ?: (customers.firstOrNull()?.id ?: "")
                        from = leave.from
                        to = leave.to ?: ""
                        note = leave.reason
                        showForm = true
                    },
                    onDeleteClick = {
                        viewModel.deleteLeave(leave.id)
                        Toast.makeText(context, viewModel.t("leaveDeleted"), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun LeaveRow(
    leave: LeaveRequest,
    customer: Customer?,
    pendingLabel: String,
    statusLabel: String,
    memberLeaveLabel: String,
    completeLabel: String,
    onCompleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BhojnifyTheme.colors
    val isOngoing = leave.to == null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .background(colors.card)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = customer?.name ?: memberLeaveLabel, color = colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (isOngoing) {
                        Badge(label = pendingLabel, tone = BadgeTone.AMBER)
                    } else {
                        Badge(
                            label = statusLabel,
                            tone = if (leave.status == LeaveStatus.APPROVED) BadgeTone.GREEN else BadgeTone.RED
                        )
                    }
                }
                Text(
                    text = "${leave.from} → ${leave.to ?: "..."}",
                    color = colors.mutedForeground,
                    fontSize = 11.sp
                )
                if (leave.reason.isNotBlank()) {
                    Text(
                        text = leave.reason,
                        color = colors.foreground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOngoing) {
                TextButton(onClick = onCompleteClick) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = colors.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = completeLabel, color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, null, tint = colors.mutedForeground, modifier = Modifier.size(17.dp))
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, null, tint = colors.mutedForeground, modifier = Modifier.size(17.dp))
            }
        }
    }
}
