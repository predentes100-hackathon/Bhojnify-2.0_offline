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
fun StaffScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val staff = state.staff
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }

    val totalPayroll = staff.sumOf { it.salary }

    val add = {
        val s = salary.toDoubleOrNull()
        if (name.trim().isEmpty() || role.trim().isEmpty() || s == null) {
            Toast.makeText(context, viewModel.t("staffRequiredMessage"), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addStaff(name.trim(), role.trim(), phone.trim().ifEmpty { viewModel.t("notAdded") }, s)
            name = ""
            role = ""
            phone = ""
            salary = ""
            Toast.makeText(context, "Team member added!", Toast.LENGTH_SHORT).show()
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
            title = viewModel.t("staffRoster"),
            subtitle = viewModel.t("staffSubtitle"),
            onActionClick = { navController.navigate(Routes.PROFILE) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.primary)
                    .padding(15.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.People, null, tint = colors.accent, modifier = Modifier.size(24.dp))
                    Text(text = "${staff.size}", color = colors.primaryForeground, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                    Text(text = viewModel.t("teamMembers"), color = colors.primaryForeground.copy(alpha = 0.76f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.accent)
                    .padding(15.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.Money, null, tint = colors.accentForeground, modifier = Modifier.size(24.dp))
                    Text(text = "₹${"%.1f".format(totalPayroll / 1000.0)}k", color = colors.accentForeground, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                    Text(text = viewModel.t("monthlyPayroll"), color = colors.accentForeground, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        SectionHeading(title = viewModel.t("currentTeam"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            if (staff.isNotEmpty()) {
                staff.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase(),
                                color = colors.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(text = member.name, color = colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${member.role} · ${member.phone}", color = colors.mutedForeground, fontSize = 10.sp)
                        }
                        Text(text = "₹${"%,.0f".format(member.salary)}", color = colors.foreground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                EmptyState(
                    icon = Icons.Default.Groups,
                    title = "No Staff Members",
                    detail = "Add chefs, helpers, and cleaning staff to track mess payroll."
                )
            }
        }

        SectionHeading(title = viewModel.t("addTeamMember"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            FormField(label = viewModel.t("fullName"), value = name, onValueChange = { name = it }, placeholder = viewModel.t("enterFullName"))
            FormField(label = viewModel.t("role"), value = role, onValueChange = { role = it }, placeholder = viewModel.t("enterRole"))
            FormField(label = viewModel.t("phoneNumber"), value = phone, onValueChange = { phone = it }, placeholder = viewModel.t("enterPhoneNumber"), keyboardType = KeyboardType.Phone)
            FormField(label = viewModel.t("monthlySalary"), value = salary, onValueChange = { salary = it }, placeholder = viewModel.t("enterMonthlySalary"), keyboardType = KeyboardType.Number)

            PrimaryButton(label = viewModel.t("addToRoster"), icon = Icons.Default.PersonAdd, onClick = add)
        }
    }
}