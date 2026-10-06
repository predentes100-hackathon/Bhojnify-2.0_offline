package com.bhojnify.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel

@Composable
fun ReportScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val summary by viewModel.reportSummary.collectAsState()

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
            title = viewModel.t("septemberReport"),
            subtitle = viewModel.t("reportSubtitle"),
            onActionClick = { navController.navigate(Routes.PROFILE) }
        )

        // Profit Surplus Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.primary)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = viewModel.t("operatingSurplus").uppercase(),
                    color = colors.primaryForeground.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
                Text(
                    text = "₹${"%,.0f".format(summary.operatingSurplus)}",
                    color = colors.primaryForeground,
                    fontSize = 33.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 5.dp)
                )
                Text(
                    text = viewModel.t("revenueLessExpenses"),
                    color = colors.primaryForeground.copy(alpha = 0.76f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Icon(Icons.Default.TrendingUp, null, tint = colors.accent, modifier = Modifier.size(34.dp))
        }

        // Stats Row
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .padding(15.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = viewModel.t("revenue").uppercase(), color = colors.mutedForeground, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    Text(text = "₹${"%,.0f".format(summary.totalRevenue)}", color = colors.foreground, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Badge(label = "+12%")
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .background(colors.card)
                    .padding(15.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = viewModel.t("expenses").uppercase(), color = colors.mutedForeground, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    Text(text = "₹${"%,.0f".format(summary.totalExpenses)}", color = colors.foreground, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Badge(label = viewModel.t("tracked"), tone = BadgeTone.GRAY)
                }
            }
        }

        SectionHeading(title = viewModel.t("serviceHealth"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = viewModel.t("mealsServed"), color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "${summary.mealsServedCount}", color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { 0.78f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = colors.primary,
                trackColor = colors.muted,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = viewModel.t("stockValueEstimate"), color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "₹${"%,.0f".format(summary.stockValueEstimate)}", color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { 0.54f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = colors.accent,
                trackColor = colors.muted,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = viewModel.t("membersRenewed"), color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "92%", color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { 0.92f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = colors.primary,
                trackColor = colors.muted,
            )
        }

        PrimaryButton(label = viewModel.t("manageExpenses"), icon = Icons.Default.Edit, secondary = true, onClick = { navController.navigate(Routes.EXPENSES) })
    }
}