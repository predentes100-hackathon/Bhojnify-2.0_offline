package com.bhojnify.android.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bhojnify.android.screens.CustomerRow
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.core.model.Customer
import com.bhojnify.core.model.CustomerPaymentStatus
import com.bhojnify.core.model.ExpiryInfo
import com.bhojnify.core.model.ExpiryStatus

// ============================================================================
// EmptyState Previews
// ============================================================================

@Preview(name = "EmptyState - Light Theme (EN)", showBackground = true)
@Composable
fun EmptyStatePreviewLight() {
    BhojnifyTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .background(BhojnifyTheme.colors.background)
                .padding(16.dp)
        ) {
            EmptyState(
                icon = Icons.Default.Inbox,
                title = "No Customers Found",
                detail = "Try adjusting your search query or filters to find what you need."
            )
        }
    }
}

@Preview(name = "EmptyState - Dark Theme (EN)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun EmptyStatePreviewDark() {
    BhojnifyTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(BhojnifyTheme.colors.background)
                .padding(16.dp)
        ) {
            EmptyState(
                icon = Icons.Default.CheckCircle,
                title = "All Stock Healthy",
                detail = "All inventory items are currently above their safety thresholds."
            )
        }
    }
}

@Preview(name = "EmptyState - Marathi (MR)", showBackground = true)
@Composable
fun EmptyStatePreviewMarathi() {
    BhojnifyTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .background(BhojnifyTheme.colors.background)
                .padding(16.dp)
        ) {
            EmptyState(
                icon = Icons.Default.CheckCircle,
                title = "सर्व साठा सुरक्षित आहे",
                detail = "कोणतेही साहित्य कमी नाही आणि सर्व काही व्यवस्थित आहे."
            )
        }
    }
}

// ============================================================================
// StatCard Previews
// ============================================================================

@Preview(name = "StatCard - Light Theme (EN)", showBackground = true)
@Composable
fun StatCardPreviewLight() {
    BhojnifyTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .background(BhojnifyTheme.colors.background)
                .padding(16.dp)
                .width(180.dp)
        ) {
            StatCard(
                label = "MEALS SERVED",
                value = "142",
                detail = "85% rate today",
                tone = BadgeTone.GREEN
            )
        }
    }
}

@Preview(name = "StatCard - Dark Theme (EN)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun StatCardPreviewDark() {
    BhojnifyTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(BhojnifyTheme.colors.background)
                .padding(16.dp)
                .width(180.dp)
        ) {
            StatCard(
                label = "REVENUE",
                value = "₹48.5k",
                detail = "This month",
                tone = BadgeTone.AMBER
            )
        }
    }
}

@Preview(name = "StatCard - Marathi (MR)", showBackground = true)
@Composable
fun StatCardPreviewMarathi() {
    BhojnifyTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .background(BhojnifyTheme.colors.background)
                .padding(16.dp)
                .width(180.dp)
        ) {
            StatCard(
                label = "सक्रिय सदस्य",
                value = "96",
                detail = "एकूण १०५",
                tone = BadgeTone.GREEN
            )
        }
    }
}

// ============================================================================
// CustomerRow Previews
// ============================================================================

@Preview(name = "CustomerRow - Active (Light EN)", showBackground = true)
@Composable
fun CustomerRowPreviewLight() {
    val sampleCustomer = Customer(
        id = "c1",
        name = "Aditya Sharma",
        plan = "Monthly Unlimited (2 Meals)",
        joiningDate = "2026-09-01",
        expiryDate = "2026-10-01",
        paymentStatus = CustomerPaymentStatus.PAID,
        phone = "9876543210",
        amount = 3000.0
    )
    val expiryInfo = ExpiryInfo(
        daysRemaining = 21,
        status = ExpiryStatus.ACTIVE,
        isExpired = false,
        isExpiringSoon = false,
        isExpiringToday = false
    )

    BhojnifyTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .background(BhojnifyTheme.colors.background)
                .padding(16.dp)
        ) {
            CustomerRow(
                customer = sampleCustomer,
                expiryInfo = expiryInfo,
                label = "21 days left",
                tone = BadgeTone.GREEN,
                localizedPaymentStatus = "Paid",
                callLabel = "Call",
                whatsAppLabel = "WhatsApp",
                renewLabel = "Renew",
                onCustomerClick = {},
                onCallClick = {},
                onWhatsAppClick = {},
                onRenewClick = {}
            )
        }
    }
}

@Preview(name = "CustomerRow - Expiring Soon (Dark EN)", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun CustomerRowPreviewDark() {
    val sampleCustomer = Customer(
        id = "c2",
        name = "Priya Deshmukh",
        plan = "15-Day Flexi Plan",
        joiningDate = "2026-09-15",
        expiryDate = "2026-09-30",
        paymentStatus = CustomerPaymentStatus.PAID,
        phone = "9823012345",
        amount = 1600.0
    )
    val expiryInfo = ExpiryInfo(
        daysRemaining = 2,
        status = ExpiryStatus.SOON,
        isExpired = false,
        isExpiringSoon = true,
        isExpiringToday = false
    )

    BhojnifyTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(BhojnifyTheme.colors.background)
                .padding(16.dp)
        ) {
            CustomerRow(
                customer = sampleCustomer,
                expiryInfo = expiryInfo,
                label = "2 days left",
                tone = BadgeTone.AMBER,
                localizedPaymentStatus = "Paid",
                callLabel = "Call",
                whatsAppLabel = "WhatsApp",
                renewLabel = "Renew",
                onCustomerClick = {},
                onCallClick = {},
                onWhatsAppClick = {},
                onRenewClick = {}
            )
        }
    }
}

@Preview(name = "CustomerRow - Marathi (MR)", showBackground = true)
@Composable
fun CustomerRowPreviewMarathi() {
    val sampleCustomer = Customer(
        id = "c3",
        name = "रोहन पाटील",
        plan = "मासिक अमर्यादित (२ जेवण)",
        joiningDate = "2026-09-01",
        expiryDate = "2026-10-01",
        paymentStatus = CustomerPaymentStatus.PAID,
        phone = "9812345678",
        amount = 3000.0
    )
    val expiryInfo = ExpiryInfo(
        daysRemaining = 15,
        status = ExpiryStatus.ACTIVE,
        isExpired = false,
        isExpiringSoon = false,
        isExpiringToday = false
    )

    BhojnifyTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .background(BhojnifyTheme.colors.background)
                .padding(16.dp)
        ) {
            CustomerRow(
                customer = sampleCustomer,
                expiryInfo = expiryInfo,
                label = "१५ दिवस शिल्लक",
                tone = BadgeTone.GREEN,
                localizedPaymentStatus = "भरले",
                callLabel = "कॉल करा",
                whatsAppLabel = "व्हॉट्सॲप",
                renewLabel = "नूतनीकरण",
                onCustomerClick = {},
                onCallClick = {},
                onWhatsAppClick = {},
                onRenewClick = {}
            )
        }
    }
}
