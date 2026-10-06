package com.bhojnify.android.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.bhojnify.core.model.CustomerPaymentStatus
import com.bhojnify.core.service.DateUtils
import java.time.LocalDate

enum class CustomerFilter { ALL, ACTIVE, EXPIRING, EXPIRED }

@Composable
fun CustomersScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val customers = state.customers
    val todayStr = DateUtils.todayString()

    var activeFilter by remember { mutableStateOf(CustomerFilter.ALL) }
    var name by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("Monthly Veg Plan") }
    var customPlan by remember { mutableStateOf("") }
    var joiningDate by remember { mutableStateOf(todayStr) }
    var expiryDate by remember { mutableStateOf(DateUtils.addDays(todayStr, 30)) }
    var phone by remember { mutableStateOf("") }
    var paymentStatus by remember { mutableStateOf(CustomerPaymentStatus.PAID) }

    val plans = listOf("Monthly Veg Plan", "Monthly Non-Veg Plan", "15-Day Flexi Plan", "Daily Meal Plan", "Custom Plan")

    val onPlanSelect = { plan: String ->
        selectedPlan = plan
        val days = when {
            plan.contains("15-Day") -> 15L
            plan.contains("Daily") -> 1L
            else -> 30L
        }
        expiryDate = DateUtils.addDays(joiningDate, days)
    }

    val add = {
        val finalPlan = if (selectedPlan == "Custom Plan") customPlan.trim() else selectedPlan
        if (name.trim().isEmpty() || finalPlan.isEmpty() || joiningDate.trim().isEmpty() || expiryDate.trim().isEmpty() || phone.trim().isEmpty()) {
            Toast.makeText(context, viewModel.t("customerRequiredMessage"), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addCustomer(
                name = name.trim(),
                plan = finalPlan,
                joiningDate = joiningDate.trim(),
                expiryDate = expiryDate.trim(),
                phone = phone.trim(),
                paymentStatus = paymentStatus
            )
            name = ""
            selectedPlan = plans[0]
            customPlan = ""
            joiningDate = todayStr
            expiryDate = DateUtils.addDays(todayStr, 30)
            phone = ""
            paymentStatus = CustomerPaymentStatus.PAID
            Toast.makeText(context, "Customer added!", Toast.LENGTH_SHORT).show()
        }
    }

    val activeCount = customers.count { DateUtils.getExpiryInfo(it.expiryDate, todayStr).status == com.bhojnify.core.model.ExpiryStatus.ACTIVE }
    val expiringCount = customers.count { DateUtils.getExpiryInfo(it.expiryDate, todayStr).isExpiringSoon }
    val expiredCount = customers.count { DateUtils.getExpiryInfo(it.expiryDate, todayStr).isExpired }

    val filteredCustomers = customers.filter {
        val info = DateUtils.getExpiryInfo(it.expiryDate, todayStr)
        when (activeFilter) {
            CustomerFilter.ACTIVE -> info.status == com.bhojnify.core.model.ExpiryStatus.ACTIVE
            CustomerFilter.EXPIRING -> info.isExpiringSoon
            CustomerFilter.EXPIRED -> info.isExpired
            CustomerFilter.ALL -> true
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
            title = viewModel.t("customerList"),
            subtitle = viewModel.t("customerListSubtitle"),
            onActionClick = { navController.navigate(Routes.ADMIN) }
        )

        // Summary Grid
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Default.People, null, tint = colors.accent, modifier = Modifier.size(20.dp))
                    Text(text = "${customers.size}", color = colors.primaryForeground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = viewModel.t("all"), color = colors.primaryForeground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.secondary)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Default.VerifiedUser, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    Text(text = "$activeCount", color = colors.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = viewModel.t("active"), color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.accent)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Default.Timer, null, tint = colors.accentForeground, modifier = Modifier.size(20.dp))
                    Text(text = "$expiringCount", color = colors.accentForeground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = viewModel.t("expiringSoon"), color = colors.accentForeground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.destructive)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Default.Warning, null, tint = colors.destructiveForeground, modifier = Modifier.size(20.dp))
                    Text(text = "$expiredCount", color = colors.destructiveForeground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = viewModel.t("expired"), color = colors.destructiveForeground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filter Tabs
        SectionHeading(title = viewModel.t("currentCustomers"))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomerFilter.values().forEach { filter ->
                val isSelected = activeFilter == filter
                val count = when (filter) {
                    CustomerFilter.ALL -> customers.size
                    CustomerFilter.ACTIVE -> activeCount
                    CustomerFilter.EXPIRING -> expiringCount
                    CustomerFilter.EXPIRED -> expiredCount
                }
                val label = when (filter) {
                    CustomerFilter.ALL -> viewModel.t("all")
                    CustomerFilter.ACTIVE -> viewModel.t("active")
                    CustomerFilter.EXPIRING -> viewModel.t("expiringSoon")
                    CustomerFilter.EXPIRED -> viewModel.t("expired")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.primary else colors.card)
                        .clickable { activeFilter = filter }
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) colors.primaryForeground else colors.foreground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.accent else colors.secondary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$count",
                                color = if (isSelected) colors.accentForeground else colors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Customer List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            if (filteredCustomers.isNotEmpty()) {
                filteredCustomers.forEach { customer ->
                    val info = DateUtils.getExpiryInfo(customer.expiryDate, todayStr)
                    val daysRemaining = info.daysRemaining
                    val isExpired = info.isExpired
                    val isExpiringSoon = info.isExpiringSoon

                    val tone = when {
                        isExpired -> BadgeTone.RED
                        isExpiringSoon -> BadgeTone.AMBER
                        else -> BadgeTone.GREEN
                    }
                    val label = when {
                        isExpired -> viewModel.t("expired")
                        daysRemaining == 0L -> viewModel.t("expiresToday")
                        isExpiringSoon -> viewModel.t("expiresInDays", mapOf("days" to daysRemaining))
                        else -> viewModel.t("daysLeft", mapOf("days" to daysRemaining))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(colors.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customer.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase(),
                                color = colors.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = customer.name, color = colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Badge(label = label, tone = tone)
                                Badge(
                                    label = viewModel.localized(customer.paymentStatus.name),
                                    tone = if (customer.paymentStatus == CustomerPaymentStatus.PAID) BadgeTone.GREEN else BadgeTone.AMBER
                                )
                            }
                            Text(text = "${customer.plan} · ${customer.phone}", color = colors.mutedForeground, fontSize = 11.sp)
                            Text(text = "${customer.joiningDate} – ${customer.expiryDate}", color = colors.mutedForeground, fontSize = 11.sp)
                        }

                        if (isExpired) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(colors.primary)
                                    .clickable { viewModel.renewCustomerPlan(customer.id) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Text(text = viewModel.t("renewPlan"), color = colors.primaryForeground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (customer.paymentStatus == CustomerPaymentStatus.UNPAID) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(colors.secondary)
                                    .clickable { viewModel.markCustomerPaid(customer.id) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Text(text = viewModel.t("markAsPaid"), color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                EmptyState(
                    icon = Icons.Default.People,
                    title = viewModel.t("noCustomers"),
                    detail = viewModel.t("noCustomersDetail")
                )
            }
        }

        // Add Customer Form
        SectionHeading(title = viewModel.t("addCustomer"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            FormField(label = viewModel.t("customerName"), value = name, onValueChange = { name = it }, placeholder = viewModel.t("enterCustomerName"))

            Text(text = viewModel.t("selectPlan"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                plans.forEach { plan ->
                    val isSelected = selectedPlan == plan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.secondary else colors.background)
                            .clickable { onPlanSelect(plan) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                null,
                                tint = if (isSelected) colors.primary else colors.mutedForeground,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(text = plan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) colors.primary else colors.foreground)
                        }
                    }
                }
            }

            if (selectedPlan == "Custom Plan") {
                FormField(label = viewModel.t("planName"), value = customPlan, onValueChange = { customPlan = it }, placeholder = viewModel.t("enterPlanName"))
            }

            FormField(label = viewModel.t("joiningDate"), value = joiningDate, onValueChange = { joiningDate = it }, placeholder = "YYYY-MM-DD")
            FormField(label = viewModel.t("expiryDate"), value = expiryDate, onValueChange = { expiryDate = it }, placeholder = "YYYY-MM-DD")
            FormField(label = viewModel.t("customerPhone"), value = phone, onValueChange = { phone = it }, placeholder = viewModel.t("enterCustomerPhone"), keyboardType = KeyboardType.Phone)

            Text(text = viewModel.t("paymentStatus"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomerPaymentStatus.values().forEach { status ->
                    val isSelected = paymentStatus == status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                            .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(13.dp))
                            .background(if (isSelected) colors.secondary else colors.background)
                            .clickable { paymentStatus = status },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = viewModel.localized(status.name), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                    }
                }
            }

            PrimaryButton(label = viewModel.t("addToCustomers"), icon = Icons.Default.PersonAdd, onClick = add)
        }
    }
}