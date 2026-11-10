package com.bhojnify.android.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import com.bhojnify.core.model.Customer
import com.bhojnify.core.model.CustomerPaymentStatus
import com.bhojnify.core.model.MessPlans
import com.bhojnify.core.service.DateUtils

enum class CustomerFilter { ALL, ACTIVE, EXPIRING, EXPIRED, UNPAID }
enum class CustomerSortBy { NAME, JOINING_DATE_NEWEST, JOINING_DATE_OLDEST, EXPIRY_DATE_EARLIEST, EXPIRY_DATE_LATEST }

@Composable
fun CustomersScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val customers = state.customers
    val todayStr = DateUtils.todayString()

    // Search & Filter & Sort State
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(CustomerFilter.ALL) }
    var sortBy by remember { mutableStateOf(CustomerSortBy.EXPIRY_DATE_EARLIEST) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Add Customer Dialog State
    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf(MessPlans.ALL.first()) }
    var customPlan by remember { mutableStateOf("") }
    var joiningDate by remember { mutableStateOf(todayStr) }
    var expiryDate by remember { mutableStateOf(DateUtils.addDays(todayStr, 30)) }
    var phone by remember { mutableStateOf("") }
    var paymentStatus by remember { mutableStateOf(CustomerPaymentStatus.PAID) }
    var initialAmount by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Customer Detail Dialog State
    var selectedCustomerForDetails by remember { mutableStateOf<Customer?>(null) }
    // Edit Customer Dialog State
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editPlan by remember { mutableStateOf("") }
    var editJoiningDate by remember { mutableStateOf("") }
    var editExpiryDate by remember { mutableStateOf("") }
    var editPaymentStatus by remember { mutableStateOf(CustomerPaymentStatus.PAID) }
    var markPaidAmount by remember { mutableStateOf("") }
    var showMarkPaidDialog by remember { mutableStateOf<Customer?>(null) }

    // Image Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val plans = MessPlans.ALL

    val onPlanSelect = { plan: String ->
        selectedPlan = plan
        val days = when {
            plan.contains("15-Day") -> 15L
            plan.contains("Daily") -> 1L
            else -> 30L
        }
        expiryDate = DateUtils.addDays(joiningDate, days)
    }

    val handleCreateCustomer = {
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
                paymentStatus = paymentStatus,
                imageUri = selectedImageUri?.toString(),
                initialAmount = initialAmount.toDoubleOrNull()
            )
            name = ""
            selectedPlan = plans.first()
            customPlan = ""
            joiningDate = todayStr
            expiryDate = DateUtils.addDays(todayStr, 30)
            phone = ""
            paymentStatus = CustomerPaymentStatus.PAID
            initialAmount = ""
            selectedImageUri = null
            showAddDialog = false
            Toast.makeText(context, "Customer registered successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    val activeCount = customers.count { DateUtils.getExpiryInfo(it.expiryDate, todayStr).status == com.bhojnify.core.model.ExpiryStatus.ACTIVE }
    val expiringCount = customers.count { DateUtils.getExpiryInfo(it.expiryDate, todayStr).isExpiringSoon }
    val expiredCount = customers.count { DateUtils.getExpiryInfo(it.expiryDate, todayStr).isExpired }
    val unpaidCount = customers.count { it.paymentStatus == CustomerPaymentStatus.UNPAID }

    val filteredCustomers = customers.filter { customer ->
        // 1. Search filter
        val q = searchQuery.trim().lowercase()
        val matchesSearch = if (q.isEmpty()) true else {
            customer.name.lowercase().contains(q) ||
            customer.phone.lowercase().contains(q) ||
            customer.plan.lowercase().contains(q)
        }

        // 2. Tab filter
        val info = DateUtils.getExpiryInfo(customer.expiryDate, todayStr)
        val matchesTab = when (activeFilter) {
            CustomerFilter.ACTIVE -> info.status == com.bhojnify.core.model.ExpiryStatus.ACTIVE
            CustomerFilter.EXPIRING -> info.isExpiringSoon
            CustomerFilter.EXPIRED -> info.isExpired
            CustomerFilter.UNPAID -> customer.paymentStatus == CustomerPaymentStatus.UNPAID
            CustomerFilter.ALL -> true
        }

        matchesSearch && matchesTab
    }.let { matching ->
        when (sortBy) {
            CustomerSortBy.NAME -> matching.sortedBy { it.name.lowercase() }
            CustomerSortBy.JOINING_DATE_NEWEST -> matching.sortedWith(
                compareByDescending<Customer> { sortableDateKey(it.joiningDate) }
                    .thenBy { it.name.lowercase() }
            )
            CustomerSortBy.JOINING_DATE_OLDEST -> matching.sortedWith(
                compareBy<Customer> { sortableDateKey(it.joiningDate) }
                    .thenBy { it.name.lowercase() }
            )
            CustomerSortBy.EXPIRY_DATE_EARLIEST -> matching.sortedWith(
                compareBy<Customer> { sortableDateKey(it.expiryDate) }
                    .thenBy { it.name.lowercase() }
            )
            CustomerSortBy.EXPIRY_DATE_LATEST -> matching.sortedWith(
                compareByDescending<Customer> { sortableDateKey(it.expiryDate) }
                    .thenBy { it.name.lowercase() }
            )
        }
    }

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
            title = viewModel.t("customers"),
            subtitle = viewModel.t("customerListSubtitle"),
            onActionClick = { navController.navigate(Routes.ADMIN) }
        )

        // Top Action Bar: "+ Add Customer" Primary Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    name = ""
                    selectedPlan = plans.first()
                    customPlan = ""
                    joiningDate = todayStr
                    expiryDate = DateUtils.addDays(todayStr, 30)
                    phone = ""
                    paymentStatus = CustomerPaymentStatus.PAID
                    selectedImageUri = null
                    showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.primaryForeground
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.t("newCustomer"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Summary Stats Grid
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, if (activeFilter == CustomerFilter.ALL) colors.primary else colors.border, RoundedCornerShape(14.dp))
                    .background(if (activeFilter == CustomerFilter.ALL) colors.primary else colors.card)
                    .clickable { activeFilter = CustomerFilter.ALL }
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        Icons.Default.People,
                        null,
                        tint = if (activeFilter == CustomerFilter.ALL) colors.accent else colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${customers.size}",
                        color = if (activeFilter == CustomerFilter.ALL) colors.primaryForeground else colors.foreground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = viewModel.t("all"),
                        color = if (activeFilter == CustomerFilter.ALL) colors.primaryForeground else colors.mutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, if (activeFilter == CustomerFilter.ACTIVE) colors.primary else colors.border, RoundedCornerShape(14.dp))
                    .background(if (activeFilter == CustomerFilter.ACTIVE) colors.secondary else colors.card)
                    .clickable { activeFilter = CustomerFilter.ACTIVE }
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Default.VerifiedUser, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Text(text = "$activeCount", color = colors.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = viewModel.t("active"), color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, if (activeFilter == CustomerFilter.EXPIRING) colors.accentForeground else colors.border, RoundedCornerShape(14.dp))
                    .background(if (activeFilter == CustomerFilter.EXPIRING) colors.accent else colors.card)
                    .clickable { activeFilter = CustomerFilter.EXPIRING }
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        Icons.Default.Timer,
                        null,
                        tint = if (activeFilter == CustomerFilter.EXPIRING) colors.accentForeground else colors.mutedForeground,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$expiringCount",
                        color = if (activeFilter == CustomerFilter.EXPIRING) colors.accentForeground else colors.foreground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = viewModel.t("expiringSoon"),
                        color = if (activeFilter == CustomerFilter.EXPIRING) colors.accentForeground else colors.mutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, if (activeFilter == CustomerFilter.EXPIRED) colors.destructive else colors.border, RoundedCornerShape(14.dp))
                    .background(if (activeFilter == CustomerFilter.EXPIRED) colors.destructive else colors.card)
                    .clickable { activeFilter = CustomerFilter.EXPIRED }
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = if (activeFilter == CustomerFilter.EXPIRED) colors.destructiveForeground else colors.destructive,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$expiredCount",
                        color = if (activeFilter == CustomerFilter.EXPIRED) colors.destructiveForeground else colors.destructive,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = viewModel.t("expired"),
                        color = if (activeFilter == CustomerFilter.EXPIRED) colors.destructiveForeground else colors.mutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(viewModel.t("searchCustomers"), color = colors.mutedForeground, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.mutedForeground) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, "Clear", tint = colors.mutedForeground)
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.card,
                unfocusedContainerColor = colors.card,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.foreground,
                unfocusedTextColor = colors.foreground
            ),
            singleLine = true
        )

        // Date-wise sorting
        Box {
            OutlinedButton(
                onClick = { showSortMenu = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.card,
                    contentColor = colors.foreground
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(sortLabel(sortBy), modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                modifier = Modifier.background(colors.card)
            ) {
                CustomerSortBy.values().forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = sortLabel(option),
                                color = if (sortBy == option) colors.primary else colors.foreground,
                                fontWeight = if (sortBy == option) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            sortBy = option
                            showSortMenu = false
                        },
                        leadingIcon = if (sortBy == option) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = colors.primary) }
                        } else null
                    )
                }
            }
        }

        // Filter Tabs
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
                    CustomerFilter.UNPAID -> unpaidCount
                }
                val label = when (filter) {
                    CustomerFilter.ALL -> viewModel.t("all")
                    CustomerFilter.ACTIVE -> viewModel.t("active")
                    CustomerFilter.EXPIRING -> viewModel.t("expiringSoon")
                    CustomerFilter.EXPIRED -> viewModel.t("expired")
                    CustomerFilter.UNPAID -> viewModel.t("unpaid")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.primary else colors.card)
                        .clickable { activeFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
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

        // Customer Directory List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                    // Customer Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                            .background(colors.card)
                            .clickable { selectedCustomerForDetails = customer }
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header row with avatar and basic info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!customer.imageUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = customer.imageUri,
                                    contentDescription = customer.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(colors.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase(),
                                        color = colors.primary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = customer.name,
                                        color = colors.foreground,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        null,
                                        tint = colors.mutedForeground,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Badge(label = label, tone = tone)
                                    Badge(
                                        label = viewModel.localized(customer.paymentStatus.displayName),
                                        tone = if (customer.paymentStatus == CustomerPaymentStatus.PAID) BadgeTone.GREEN else BadgeTone.AMBER
                                    )
                                }
                            }
                        }

                        // Meta row (Plan and Dates)
                        Divider(color = colors.border, thickness = 0.5.dp)
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.Restaurant, null, tint = colors.mutedForeground, modifier = Modifier.size(13.dp))
                                Text(text = customer.plan, color = colors.foreground, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.CalendarToday, null, tint = colors.mutedForeground, modifier = Modifier.size(13.dp))
                                Text(text = "${customer.joiningDate} → ${customer.expiryDate}", color = colors.mutedForeground, fontSize = 12.sp)
                            }
                        }

                        // Card Quick Action Buttons
                        Divider(color = colors.border, thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Call Button
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.primary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Call, null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = viewModel.t("callCustomer"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // WhatsApp Button
                            Button(
                                onClick = {
                                    val cleanPhone = customer.phone.replace(Regex("[^0-9]"), "").let {
                                        if (it.length == 10) "91$it" else it
                                    }
                                    val url = "https://wa.me/$cleanPhone?text=Hello%20${Uri.encode(customer.name)}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Chat, null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = viewModel.t("whatsappCustomer"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Renew Button
                            Button(
                                onClick = {
                                    viewModel.renewCustomerPlan(customer.id)
                                    Toast.makeText(context, "Plan renewed successfully!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.primaryForeground),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = viewModel.t("renewPlan"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (searchQuery.isNotEmpty()) {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = viewModel.t("noSearchResults"),
                    detail = viewModel.t("noSearchResultsDetail")
                )
            } else {
                EmptyState(
                    icon = Icons.Default.People,
                    title = viewModel.t("noCustomers"),
                    detail = viewModel.t("noCustomersDetail")
                )
            }
        }
    }

    // ADD CUSTOMER DIALOG
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.card,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.t("addNewCustomer"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                        IconButton(onClick = { showAddDialog = false }) {
                            Icon(Icons.Default.Close, "Close", tint = colors.mutedForeground)
                        }
                    }

                    // Photo Picker in Dialog
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (selectedImageUri != null) colors.primary else colors.border, CircleShape)
                                .background(colors.secondary)
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Customer Photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.AddAPhoto, "Add Photo", tint = colors.primary, modifier = Modifier.size(32.dp))
                            }
                        }

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.primary),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (selectedImageUri != null) viewModel.t("changePhoto") else viewModel.t("addCustomerPhoto"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FormField(
                        label = viewModel.t("customerName"),
                        value = name,
                        onValueChange = { name = it },
                        placeholder = viewModel.t("enterCustomerName")
                    )

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
                    FormField(label = "Onboarding payment (₹)", value = initialAmount, onValueChange = { initialAmount = it }, placeholder = "Optional amount", keyboardType = KeyboardType.Number)

                    Text(text = viewModel.t("paymentStatus"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CustomerPaymentStatus.values().forEach { status ->
                            val isSelected = paymentStatus == status
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(12.dp))
                                    .background(if (isSelected) colors.secondary else colors.background)
                                    .clickable { paymentStatus = status },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = viewModel.localized(status.displayName), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.foreground),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Text(text = viewModel.t("cancel"), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = handleCreateCustomer,
                            modifier = Modifier.weight(2f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.primaryForeground),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = viewModel.t("addToCustomers"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // CUSTOMER DETAILS DIALOG
    selectedCustomerForDetails?.let { customer ->
        Dialog(onDismissRequest = { selectedCustomerForDetails = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.card,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.t("customerDetails"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                        IconButton(onClick = { selectedCustomerForDetails = null }) {
                            Icon(Icons.Default.Close, "Close", tint = colors.mutedForeground)
                        }
                    }

                    // Avatar & Name
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!customer.imageUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = customer.imageUri,
                                contentDescription = customer.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(colors.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customer.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase(),
                                    color = colors.primary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = customer.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.foreground
                        )
                        Text(
                            text = customer.phone,
                            fontSize = 13.sp,
                            color = colors.mutedForeground
                        )
                    }

                    // Direct Action Buttons (Call & WhatsApp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Call, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = viewModel.t("callCustomer"), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val cleanPhone = customer.phone.replace(Regex("[^0-9]"), "").let {
                                    if (it.length == 10) "91$it" else it
                                }
                                val url = "https://wa.me/$cleanPhone?text=Hello%20${Uri.encode(customer.name)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Chat, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = viewModel.t("whatsappCustomer"), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Subscription details card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .background(colors.background)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = viewModel.t("subscriptionDetails"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${viewModel.t("planName")}:", fontSize = 12.sp, color = colors.mutedForeground)
                            Text(text = customer.plan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.foreground)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${viewModel.t("paymentStatus")}:", fontSize = 12.sp, color = colors.mutedForeground)
                            Badge(
                                label = viewModel.localized(customer.paymentStatus.displayName),
                                tone = if (customer.paymentStatus == CustomerPaymentStatus.PAID) BadgeTone.GREEN else BadgeTone.AMBER
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${viewModel.t("joiningDate")}:", fontSize = 12.sp, color = colors.mutedForeground)
                            Text(text = customer.joiningDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.foreground)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${viewModel.t("expiryDate")}:", fontSize = 12.sp, color = colors.mutedForeground)
                            Text(text = customer.expiryDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.foreground)
                        }
                    }

                    // Renew & Delete Action Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                editName = customer.name
                                editPhone = customer.phone
                                editPlan = customer.plan
                                editJoiningDate = customer.joiningDate
                                editExpiryDate = customer.expiryDate
                                editPaymentStatus = customer.paymentStatus
                                customerToEdit = customer
                                selectedCustomerForDetails = null
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.foreground),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Edit info", fontWeight = FontWeight.Bold)
                        }

                        if (customer.paymentStatus == CustomerPaymentStatus.UNPAID) {
                            Button(
                                onClick = {
                                    markPaidAmount = ""
                                    showMarkPaidDialog = customer
                                    selectedCustomerForDetails = null
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.primaryForeground),
                                shape = RoundedCornerShape(13.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Mark as paid", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.renewCustomerPlan(customer.id)
                                Toast.makeText(context, "Plan renewed successfully!", Toast.LENGTH_SHORT).show()
                                selectedCustomerForDetails = null
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.primaryForeground),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = viewModel.t("renewPlan"), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.deleteCustomer(customer.id)
                                Toast.makeText(context, "Customer removed", Toast.LENGTH_SHORT).show()
                                selectedCustomerForDetails = null
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.destructive, contentColor = colors.destructiveForeground),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = viewModel.t("deleteCustomer"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // EDIT CUSTOMER DIALOG
    customerToEdit?.let { original ->
        Dialog(onDismissRequest = { customerToEdit = null }) {
            Surface(shape = RoundedCornerShape(22.dp), color = colors.card, modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Edit customer", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                        IconButton(onClick = { customerToEdit = null }) { Icon(Icons.Default.Close, "Close", tint = colors.mutedForeground) }
                    }
                    FormField(label = "Name", value = editName, onValueChange = { editName = it }, placeholder = "Customer name")
                    FormField(label = "Phone", value = editPhone, onValueChange = { editPhone = it }, placeholder = "Phone number", keyboardType = KeyboardType.Phone)
                    FormField(label = "Plan", value = editPlan, onValueChange = { editPlan = it }, placeholder = "Plan name")
                    FormField(label = "Joining date (YYYY-MM-DD)", value = editJoiningDate, onValueChange = { editJoiningDate = it }, placeholder = "YYYY-MM-DD")
                    FormField(label = "Expiry date (YYYY-MM-DD)", value = editExpiryDate, onValueChange = { editExpiryDate = it }, placeholder = "YYYY-MM-DD")
                    Text("Payment status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CustomerPaymentStatus.values().forEach { status ->
                            val isSel = editPaymentStatus == status
                            Box(modifier = Modifier.weight(1f).height(44.dp).border(1.dp, if (isSel) colors.primary else colors.border, RoundedCornerShape(12.dp)).background(if (isSel) colors.secondary else colors.background).clickable { editPaymentStatus = status }, contentAlignment = Alignment.Center) {
                                Text(text = viewModel.localized(status.displayName), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { customerToEdit = null }, modifier = Modifier.weight(1f).height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.foreground), shape = RoundedCornerShape(13.dp)) { Text("Cancel", fontWeight = FontWeight.Bold) }
                        Button(
                            onClick = {
                                if (editName.isNotBlank() && editPhone.isNotBlank()) {
                                    viewModel.updateCustomer(original.id) { it.copy(name = editName.trim(), phone = editPhone.trim(), plan = editPlan.trim(), joiningDate = editJoiningDate.trim(), expiryDate = editExpiryDate.trim(), paymentStatus = editPaymentStatus) }
                                    Toast.makeText(context, "Customer updated", Toast.LENGTH_SHORT).show()
                                    customerToEdit = null
                                } else {
                                    Toast.makeText(context, "Name and phone are required", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(2f).height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.primaryForeground), shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // MARK AS PAID DIALOG
    showMarkPaidDialog?.let { customer ->
        Dialog(onDismissRequest = { showMarkPaidDialog = null }) {
            Surface(shape = RoundedCornerShape(22.dp), color = colors.card, modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Mark as paid — ${customer.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.foreground)
                    FormField(label = "Amount received (₹)", value = markPaidAmount, onValueChange = { markPaidAmount = it }, placeholder = "Enter amount", keyboardType = KeyboardType.Number)
                    Text("Leave blank to just mark paid without recording a payment.", color = colors.mutedForeground, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { showMarkPaidDialog = null }, modifier = Modifier.weight(1f).height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.foreground), shape = RoundedCornerShape(13.dp)) { Text("Cancel", fontWeight = FontWeight.Bold) }
                        Button(
                            onClick = {
                                viewModel.markCustomerPaid(customer.id, markPaidAmount.toDoubleOrNull())
                                Toast.makeText(context, "Marked as paid", Toast.LENGTH_SHORT).show()
                                showMarkPaidDialog = null
                            },
                            modifier = Modifier.weight(2f).height(46.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.primaryForeground), shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm paid", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun sortableDateKey(dateStr: String): String {
    return if (dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) dateStr else "0000-00-00"
}

private fun sortLabel(sortBy: CustomerSortBy): String {
    return when (sortBy) {
        CustomerSortBy.NAME -> "Sort by Name (A-Z)"
        CustomerSortBy.JOINING_DATE_NEWEST -> "Joining Date: Newest First"
        CustomerSortBy.JOINING_DATE_OLDEST -> "Joining Date: Oldest First"
        CustomerSortBy.EXPIRY_DATE_EARLIEST -> "Expiry Date: Earliest First"
        CustomerSortBy.EXPIRY_DATE_LATEST -> "Expiry Date: Latest First"
    }
}
