package com.bhojnify.android.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bhojnify.android.components.EmptyState
import com.bhojnify.android.components.FormField
import com.bhojnify.android.components.Header
import com.bhojnify.android.components.PrimaryButton
import com.bhojnify.android.components.SectionHeading
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import com.bhojnify.core.model.Customer
import com.bhojnify.core.model.Payment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val payments = state.payments
    val customers = state.customers

    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("UPI") }
    var note by remember { mutableStateOf("") }
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val selectedCustomer = customers.find { it.id == selectedCustomerId }

    val amountDouble = amount.toDoubleOrNull()
    val isAmountError = amount.isNotBlank() && (amountDouble == null || amountDouble <= 0.0)

    val add = {
        val a = amount.toDoubleOrNull()
        if (a == null || a <= 0.0 || note.trim().isEmpty()) {
            Toast.makeText(context, viewModel.t("paymentRequiredMessage"), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addPayment(a, method.trim(), note.trim(), selectedCustomerId)
            amount = ""
            note = ""
            selectedCustomerId = null
            Toast.makeText(context, viewModel.t("paymentRecordedMessage"), Toast.LENGTH_SHORT).show()
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
                title = viewModel.t("paymentLedger"),
                subtitle = viewModel.t("paymentSubtitle"),
                actionContentDescription = viewModel.t("profile"),
                onActionClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        item(key = "ledger_banner") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(21.dp))
                    .background(colors.primary)
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = colors.accent, modifier = Modifier.size(30.dp))
                Column {
                    Text(text = viewModel.t("ledgerUpToDate"), color = colors.primaryForeground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = viewModel.t("recordsStayOnDevice"), color = colors.primaryForeground.copy(alpha = 0.76f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        item(key = "past_payments_heading") {
            SectionHeading(title = viewModel.t("pastPayments"))
        }

        if (payments.isEmpty()) {
            item(key = "empty_state") {
                EmptyState(
                    icon = Icons.Default.Payment,
                    title = viewModel.t("noPaymentsFound"),
                    detail = viewModel.t("noPaymentsFoundDetail")
                )
            }
        } else {
            items(payments, key = { it.id }) { payment ->
                PaymentRow(payment = payment, localizedMethod = viewModel.localized(payment.method))
            }
        }

        item(key = "record_payment_heading") {
            SectionHeading(title = viewModel.t("recordPayment"))
        }

        item(key = "record_payment_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                    .background(colors.card)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Optional Customer Picker Dropdown
                if (customers.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = viewModel.t("chooseCustomer"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                        Box {
                            OutlinedButton(
                                onClick = { showCustomerPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(13.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = colors.background,
                                    contentColor = colors.foreground
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = selectedCustomer?.name ?: viewModel.t("chooseCustomer"),
                                    color = if (selectedCustomer != null) colors.foreground else colors.mutedForeground,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showCustomerPicker,
                                onDismissRequest = { showCustomerPicker = false },
                                modifier = Modifier.background(colors.card)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(viewModel.t("all"), color = colors.foreground) },
                                    onClick = {
                                        selectedCustomerId = null
                                        showCustomerPicker = false
                                    }
                                )
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = { Text("${cust.name} (${cust.plan})", color = colors.foreground) },
                                        onClick = {
                                            selectedCustomerId = cust.id
                                            showCustomerPicker = false
                                            if (note.isBlank()) {
                                                note = "${cust.name} - ${cust.plan}"
                                            }
                                            cust.amount?.let { amt ->
                                                amount = amt.toInt().toString()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Amount Field with Inline Validation
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = viewModel.t("amount"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.foreground
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = { Text(viewModel.t("enterPaymentAmount"), color = colors.mutedForeground, fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(13.dp),
                        isError = isAmountError,
                        supportingText = if (isAmountError) {
                            { Text(viewModel.t("enterValidAmount"), color = colors.destructive) }
                        } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.card,
                            unfocusedContainerColor = colors.card,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.input,
                            focusedTextColor = colors.foreground,
                            unfocusedTextColor = colors.foreground
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Method selection with FilterChips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = viewModel.t("method"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.foreground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("UPI", "Cash", "Card").forEach { opt ->
                            val isSel = method == opt
                            FilterChip(
                                selected = isSel,
                                onClick = { method = opt },
                                label = { Text(viewModel.localized(opt), fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isSel) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                FormField(
                    label = viewModel.t("note"),
                    value = note,
                    onValueChange = { note = it },
                    placeholder = viewModel.t("enterPaymentNote")
                )

                PrimaryButton(
                    label = viewModel.t("recordPaymentButton"),
                    icon = Icons.Default.Check,
                    onClick = add
                )
            }
        }
    }
}

@Composable
fun PaymentRow(
    payment: Payment,
    localizedMethod: String,
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
                .background(colors.secondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Receipt, null, tint = colors.primary, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = payment.note, color = colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "${payment.date} · $localizedMethod", color = colors.mutedForeground, fontSize = 11.sp)
        }
        Text(text = "₹${"%,.0f".format(payment.amount)}", color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
