package com.bhojnify.android.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import com.bhojnify.core.model.StaffMember

@Composable
fun StaffScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val staff = state.staff
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }

    val totalPayroll = remember(staff) { staff.sumOf { it.salary } }

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
            Toast.makeText(context, viewModel.t("teamMemberAdded"), Toast.LENGTH_SHORT).show()
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
                title = viewModel.t("staffRoster"),
                subtitle = viewModel.t("staffSubtitle"),
                actionContentDescription = viewModel.t("profile"),
                onActionClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        item(key = "stats") {
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
        }

        item(key = "team_heading") {
            SectionHeading(title = viewModel.t("currentTeam"))
        }

        if (staff.isEmpty()) {
            item(key = "empty_staff") {
                EmptyState(
                    icon = Icons.Default.Groups,
                    title = viewModel.t("noStaffMembers"),
                    detail = viewModel.t("noStaffMembersDetail")
                )
            }
        } else {
            items(staff, key = { it.id }) { member ->
                StaffRow(member = member)
            }
        }

        item(key = "add_team_heading") {
            SectionHeading(title = viewModel.t("addTeamMember"))
        }

        item(key = "add_team_form") {
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
}

@Composable
fun StaffRow(
    member: StaffMember,
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
            Text(text = "${member.role} · ${member.phone}", color = colors.mutedForeground, fontSize = 11.sp)
        }
        Text(text = "₹${"%,.0f".format(member.salary)}", color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
