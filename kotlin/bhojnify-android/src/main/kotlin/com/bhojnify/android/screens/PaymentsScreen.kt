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
fun PaymentsScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val payments = state.payments
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("UPI") }
    var note by remember { mutableStateOf("") }

    val add = {
        val a = amount.toDoubleOrNull()
        if (a == null || note.trim().isEmpty()) {
            Toast.makeText(context, viewModel.t("paymentRequiredMessage"), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addPayment(a, method.trim(), note.trim())
            amount = ""
            note = ""
            Toast.makeText(context, viewModel.t("paymentRecordedMessage"), Toast.LENGTH_SHORT).show()
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
            title = viewModel.t("paymentLedger"),
            subtitle = viewModel.t("paymentSubtitle"),
            onActionClick = { navController.navigate(Routes.PROFILE) }
        )

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

        SectionHeading(title = viewModel.t("pastPayments"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            if (payments.isNotEmpty()) {
                payments.forEach { payment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
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
                            Text(text = "${payment.date} · ${viewModel.localized(payment.method)}", color = colors.mutedForeground, fontSize = 11.sp)
                        }
                        Text(text = "₹${"%,.0f".format(payment.amount)}", color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                EmptyState(
                    icon = Icons.Default.Payment,
                    title = "No Payments Found",
                    detail = "Received fees from customers will show up here."
                )
            }
        }

        SectionHeading(title = viewModel.t("recordPayment"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            FormField(label = viewModel.t("amount"), value = amount, onValueChange = { amount = it }, placeholder = viewModel.t("enterPaymentAmount"), keyboardType = KeyboardType.Number)
            FormField(label = viewModel.t("method"), value = method, onValueChange = { method = it }, placeholder = viewModel.t("enterPaymentMethod"))
            FormField(label = viewModel.t("note"), value = note, onValueChange = { note = it }, placeholder = viewModel.t("enterPaymentNote"))

            PrimaryButton(label = viewModel.t("recordPaymentButton"), icon = Icons.Default.Check, onClick = add)
        }
    }
}