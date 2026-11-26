package com.bhojnify.android.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.core.model.Customer
import com.bhojnify.core.model.CustomerPaymentStatus
import com.bhojnify.core.model.ExpiryInfo
import com.bhojnify.core.service.DateUtils

/**
 * Authentic physical Indian mess / khata register styling tokens.
 */
private object LedgerColors {
    // Parchment / Cream paper backgrounds
    val paperLight = Color(0xFFFAF7EE) // Warm vintage mess register cream
    val paperDark = Color(0xFF16201A)  // Deep slate antique register paper
    val headerLight = Color(0xFFF1EAD9) // Shaded title row paper
    val headerDark = Color(0xFF1E2D24)

    // Ruled horizontal notebook guidelines
    val ruledLineLight = Color(0xFFD4E2EE) // Feint blue notebook ruling
    val ruledLineDark = Color(0xFF26382E)

    // Classic red margin index rule (double vertical line)
    val marginRedLight = Color(0xFFD32F2F) // Crimson red ledger margin
    val marginRedDark = Color(0xFFEF5350).copy(alpha = 0.72f)

    // Handbound notebook spine strip
    val spineColorLight = Color(0xFF5C2024) // Deep maroon cloth spine
    val spineColorDark = Color(0xFF381417)
    val stitchLight = Color(0xFFD5A267).copy(alpha = 0.55f) // Bookbinding thread stitch

    // Traditional ballpoint ink colors
    val inkPrimaryLight = Color(0xFF17352B) // Deep ink black/green
    val inkPrimaryDark = Color(0xFFF0F5F1)
    val inkMutedLight = Color(0xFF5E6F67)   // Secondary register ink
    val inkMutedDark = Color(0xFFA2B4A9)

    // Rubber ink stamps (PAID / DUE)
    val stampPaidLight = Color(0xFF1D7A58) // Green approval ink
    val stampPaidDark = Color(0xFF82D0A8)
    val stampDueLight = Color(0xFFC85545)  // Crimson overdue ink
    val stampDueDark = Color(0xFFEA8574)
}

// Column width specifications (Total width = 810.dp)
private val SpineWidth = 8.dp
private val SrNoWidth = 46.dp
private val MarginWidth = 6.dp
private val NamePhoneWidth = 180.dp
private val PlanWidth = 135.dp
private val JoiningDateWidth = 105.dp
private val ExpiryDateWidth = 120.dp
private val PaymentStatusWidth = 100.dp
private val ActionsWidth = 110.dp
private val TotalTableWidth = SpineWidth + SrNoWidth + MarginWidth + NamePhoneWidth +
        PlanWidth + JoiningDateWidth + ExpiryDateWidth + PaymentStatusWidth + ActionsWidth

/**
 * CustomerRegisterView renders the customer directory as an authentic traditional
 * Indian mess / khata register (खाता नोंदवही) with ruled notebook lines, red margin,
 * rubber-stamp payment indicators, and compact tabular typography.
 */
@Composable
fun CustomerRegisterView(
    customers: List<Customer>,
    customerExpiryMap: Map<String, ExpiryInfo>,
    todayStr: String,
    onCustomerClick: (Customer) -> Unit,
    onCallClick: (Customer) -> Unit,
    onWhatsAppClick: (Customer) -> Unit,
    onEditClick: (Customer) -> Unit,
    t: (String) -> String,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val paperBg = if (dark) LedgerColors.paperDark else LedgerColors.paperLight
    val borderColor = if (dark) Color(0xFF2B3C31) else Color(0xFFDED6C4)
    val scrollState = rememberScrollState()

    // Outer Ledger Book Canvas Card
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(paperBg)
    ) {
        if (customers.isEmpty()) {
            RegisterEmptyState(t = t)
        } else {
            // Horizontal scroll container allowing smooth panning across columns
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Column(modifier = Modifier.width(TotalTableWidth)) {
                    // Printed Header Row
                    RegisterTableHeader(t = t, dark = dark)

                    // Tabular Customer Ledger Rows
                    customers.forEachIndexed { index, customer ->
                        val expiryInfo = customerExpiryMap[customer.id]
                            ?: DateUtils.getExpiryInfo(customer.expiryDate, todayStr)

                        RegisterTableRow(
                            srNo = index + 1,
                            customer = customer,
                            expiryInfo = expiryInfo,
                            dark = dark,
                            t = t,
                            onCustomerClick = { onCustomerClick(customer) },
                            onCallClick = { onCallClick(customer) },
                            onWhatsAppClick = { onWhatsAppClick(customer) },
                            onEditClick = { onEditClick(customer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterTableHeader(t: (String) -> String, dark: Boolean) {
    val headerBg = if (dark) LedgerColors.headerDark else LedgerColors.headerLight
    val headerInk = if (dark) LedgerColors.inkPrimaryDark else LedgerColors.inkPrimaryLight
    val ruleColor = if (dark) LedgerColors.ruledLineDark else LedgerColors.ruledLineLight

    Column(
        modifier = Modifier
            .width(TotalTableWidth)
            .background(headerBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Book Binder Spine Top Edge
            Box(
                modifier = Modifier
                    .width(SpineWidth)
                    .fillMaxHeight()
                    .background(if (dark) LedgerColors.spineColorDark else LedgerColors.spineColorLight)
            )

            // Sr No Header (#)
            Text(
                text = t("srNo"),
                modifier = Modifier.width(SrNoWidth),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = headerInk
            )

            // Red Vertical Double Margin Rule
            DoubleRedMarginLine(dark = dark)

            // Name & Phone Header
            Text(
                text = t("nameAndPhone"),
                modifier = Modifier
                    .width(NamePhoneWidth)
                    .padding(horizontal = 8.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = headerInk
            )

            // Plan Header
            Text(
                text = t("planName"),
                modifier = Modifier
                    .width(PlanWidth)
                    .padding(horizontal = 8.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = headerInk
            )

            // Joining Date Header
            Text(
                text = t("joiningDate"),
                modifier = Modifier
                    .width(JoiningDateWidth)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = headerInk
            )

            // Expiry Date Header
            Text(
                text = t("expiryDate"),
                modifier = Modifier
                    .width(ExpiryDateWidth)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = headerInk
            )

            // Payment Status Header
            Text(
                text = t("paymentStatus"),
                modifier = Modifier.width(PaymentStatusWidth),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = headerInk
            )

            // Actions Header
            Text(
                text = t("actions"),
                modifier = Modifier.width(ActionsWidth),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = headerInk
            )
        }

        // Classic Double Ruled Ledger Title Border
        HorizontalDivider(thickness = 1.dp, color = ruleColor)
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(thickness = 1.dp, color = ruleColor)
    }
}

@Composable
private fun RegisterTableRow(
    srNo: Int,
    customer: Customer,
    expiryInfo: ExpiryInfo,
    dark: Boolean,
    t: (String) -> String,
    onCustomerClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val inkPrimary = if (dark) LedgerColors.inkPrimaryDark else LedgerColors.inkPrimaryLight
    val inkMuted = if (dark) LedgerColors.inkMutedDark else LedgerColors.inkMutedLight
    val ruleColor = if (dark) LedgerColors.ruledLineDark else LedgerColors.ruledLineLight

    Column(
        modifier = Modifier
            .width(TotalTableWidth)
            .clickable { onCustomerClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Book Binder Spine with Stitch Notch
            Box(
                modifier = Modifier
                    .width(SpineWidth)
                    .fillMaxHeight()
                    .background(if (dark) LedgerColors.spineColorDark else LedgerColors.spineColorLight),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = 8.dp)
                        .background(LedgerColors.stitchLight)
                )
            }

            // Sr No (#)
            Text(
                text = "$srNo",
                modifier = Modifier.width(SrNoWidth),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = inkPrimary
            )

            // Red Vertical Double Margin Rule
            DoubleRedMarginLine(dark = dark)

            // Name & Phone
            Column(
                modifier = Modifier
                    .width(NamePhoneWidth)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = customer.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = inkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = customer.phone,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Plan
            Text(
                text = customer.plan,
                modifier = Modifier
                    .width(PlanWidth)
                    .padding(horizontal = 8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = inkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Joining Date
            Text(
                text = customer.joiningDate,
                modifier = Modifier
                    .width(JoiningDateWidth)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = inkMuted,
                maxLines = 1
            )

            // Expiry Date + Compact Inline Status Pill
            Column(
                modifier = Modifier
                    .width(ExpiryDateWidth)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = customer.expiryDate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = inkPrimary,
                    maxLines = 1
                )
                val statusText = when {
                    expiryInfo.isExpired -> t("expired")
                    expiryInfo.isExpiringToday -> t("expiresToday")
                    expiryInfo.isExpiringSoon -> "${expiryInfo.daysRemaining}d left"
                    else -> "${expiryInfo.daysRemaining}d"
                }
                val statusColor = when {
                    expiryInfo.isExpired -> if (dark) LedgerColors.stampDueDark else LedgerColors.stampDueLight
                    expiryInfo.isExpiringSoon -> BhojnifyTheme.colors.accentForeground
                    else -> if (dark) LedgerColors.stampPaidDark else LedgerColors.stampPaidLight
                }
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    maxLines = 1
                )
            }

            // Payment Status (Rubber Ink Stamp)
            Box(
                modifier = Modifier
                    .width(PaymentStatusWidth)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                InkStampBadge(
                    paymentStatus = customer.paymentStatus,
                    dark = dark,
                    t = t
                )
            }

            // Actions (Call, WhatsApp, Edit)
            Row(
                modifier = Modifier
                    .width(ActionsWidth)
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = t("callCustomer"),
                        tint = BhojnifyTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onWhatsAppClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = t("whatsappCustomer"),
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = t("editInfo"),
                        tint = BhojnifyTheme.colors.mutedForeground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Feint Blue Ruled Line Between Ledger Entries
        HorizontalDivider(thickness = 0.8.dp, color = ruleColor)
    }
}

@Composable
private fun DoubleRedMarginLine(dark: Boolean) {
    val marginColor = if (dark) LedgerColors.marginRedDark else LedgerColors.marginRedLight
    Row(
        modifier = Modifier
            .width(MarginWidth)
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .fillMaxHeight()
                .background(marginColor)
        )
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .fillMaxHeight()
                .background(marginColor)
        )
    }
}

@Composable
private fun InkStampBadge(
    paymentStatus: CustomerPaymentStatus,
    dark: Boolean,
    t: (String) -> String
) {
    val isPaid = paymentStatus == CustomerPaymentStatus.PAID
    val stampColor = if (isPaid) {
        if (dark) LedgerColors.stampPaidDark else LedgerColors.stampPaidLight
    } else {
        if (dark) LedgerColors.stampDueDark else LedgerColors.stampDueLight
    }
    val stampLabel = if (isPaid) t("paid").uppercase() else t("unpaid").uppercase()

    Box(
        modifier = Modifier
            .border(
                width = 1.2.dp,
                color = stampColor.copy(alpha = 0.85f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(stampColor.copy(alpha = 0.08f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stampLabel,
            color = stampColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun RegisterEmptyState(t: (String) -> String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = BhojnifyTheme.colors.mutedForeground,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = t("noSearchResults"),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BhojnifyTheme.colors.foreground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = t("noSearchResultsDetail"),
            fontSize = 12.sp,
            color = BhojnifyTheme.colors.mutedForeground,
            textAlign = TextAlign.Center
        )
    }
}
