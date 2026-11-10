package com.bhojnify.android.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import com.bhojnify.core.i18n.Language

@Composable
fun ProfileScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val profile = state.profile
    val policies = state.policies

    var rules by remember(policies.rules) { mutableStateOf(policies.rules) }
    var privacy by remember(policies.privacy) { mutableStateOf(policies.privacy) }
    var maxPlanValidity by remember(policies.maxPlanValidityDays) { mutableStateOf(policies.maxPlanValidityDays.toString()) }
    var maxLeaveDays by remember(policies.maxLeaveDays) { mutableStateOf(policies.maxLeaveDays.toString()) }

    val initials = if (profile.name.isNotEmpty()) {
        profile.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
    } else "OW"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header(
            eyebrow = viewModel.t("ownerAccount"),
            title = viewModel.t("profileTitle"),
            subtitle = viewModel.t("profileSubtitle")
        )

        // Profile Identity Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = colors.primaryForeground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = profile.name.ifEmpty { viewModel.t("owner") },
                    color = colors.foreground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${viewModel.t("messOwner")} · ${profile.messName.ifEmpty { viewModel.t("yourMess") }}",
                    color = colors.mutedForeground,
                    fontSize = 12.sp
                )
            }
            Badge(label = viewModel.t("owner"), tone = BadgeTone.GREEN)
        }

        SectionHeading(title = viewModel.t("ownerShortcuts"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 14.dp)
        ) {
            RowItem(
                icon = Icons.Default.GridOn,
                title = viewModel.t("operationsPanel"),
                detail = viewModel.t("operationsDetail"),
                onClick = { navController.navigate(Routes.ADMIN) }
            )
            RowItem(
                icon = Icons.Default.Inventory,
                title = viewModel.t("inventoryLedger"),
                detail = viewModel.t("inventoryDetail"),
                onClick = { navController.navigate(Routes.INVENTORY) }
            )
            RowItem(
                icon = Icons.Default.CalendarToday,
                title = viewModel.t("menuPlanner"),
                detail = viewModel.t("menuDetail"),
                onClick = { navController.navigate(Routes.MENU) }
            )
            RowItem(
                icon = Icons.Default.BarChart,
                title = viewModel.t("septemberReport"),
                detail = viewModel.t("reportDetail"),
                onClick = { navController.navigate(Routes.REPORT) }
            )
        }

        SectionHeading(title = viewModel.t("localWorkspace"))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.secondary)
                .padding(15.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.PhoneAndroid, null, tint = colors.primary, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = viewModel.t("savedOnDevice"),
                    color = colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.t("localStorageDetail"),
                    color = colors.secondaryForeground,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        SectionHeading(title = viewModel.t("policies"))
        Text(
            text = viewModel.t("policiesSubtitle"),
            color = colors.mutedForeground,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.offset(y = (-10).dp)
        )

        // Rules Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Text(
                text = viewModel.t("rulesAndRegulations"),
                color = colors.foreground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = rules,
                onValueChange = {
                    rules = it
                    viewModel.updatePolicies(policies.copy(rules = it))
                },
                placeholder = { Text(viewModel.t("rulesPlaceholder")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.background,
                    unfocusedContainerColor = colors.background,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.input
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(colors.secondary)
                    .clickable {
                        if (rules.isNotBlank()) {
                            clipboardManager.setText(AnnotatedString(rules))
                            Toast.makeText(context, viewModel.t("savedOnDevice"), Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = viewModel.t("copyRules"),
                        color = colors.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Privacy Policy Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Text(
                text = viewModel.t("privacyPolicy"),
                color = colors.foreground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = privacy,
                onValueChange = {
                    privacy = it
                    viewModel.updatePolicies(policies.copy(privacy = it))
                },
                placeholder = { Text(viewModel.t("privacyPolicyPlaceholder")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.background,
                    unfocusedContainerColor = colors.background,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.input
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(colors.secondary)
                    .clickable {
                        if (privacy.isNotBlank()) {
                            clipboardManager.setText(AnnotatedString(privacy))
                            Toast.makeText(context, viewModel.t("savedOnDevice"), Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = viewModel.t("copyPrivacyPolicy"),
                        color = colors.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        SectionHeading(title = viewModel.t("planAndLeaveLimits"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(viewModel.t("planLeaveLimitsDetail"), color = colors.mutedForeground, fontSize = 12.sp)
            OutlinedTextField(
                value = maxPlanValidity,
                onValueChange = {
                    maxPlanValidity = it
                    it.toIntOrNull()?.takeIf { value -> value > 0 }?.let { value -> viewModel.updatePolicies(policies.copy(maxPlanValidityDays = value)) }
                },
                label = { Text(viewModel.t("maxPlanValidityDays")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = maxLeaveDays,
                onValueChange = {
                    maxLeaveDays = it
                    it.toIntOrNull()?.takeIf { value -> value >= 0 }?.let { value -> viewModel.updatePolicies(policies.copy(maxLeaveDays = value)) }
                },
                label = { Text(viewModel.t("maxLeaveDays")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(viewModel.t("limitsDefaults"), color = colors.mutedForeground, fontSize = 11.sp)
        }


        SectionHeading(title = viewModel.t("language"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                .background(colors.card)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = viewModel.t("chooseLanguage"), fontSize = 12.sp, color = colors.mutedForeground)
            Row(
                modifier = Modifier
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .background(colors.background)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isEn = state.language == Language.EN
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isEn) colors.primary else Color.Transparent)
                        .clickable { viewModel.setLanguage(Language.EN) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = viewModel.t("english"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEn) colors.primaryForeground else colors.foreground
                    )
                }
                val isMr = state.language == Language.MR
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isMr) colors.primary else Color.Transparent)
                        .clickable { viewModel.setLanguage(Language.MR) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = viewModel.t("marathi"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMr) colors.primaryForeground else colors.foreground
                    )
                }
            }
        }

        SectionHeading(title = viewModel.t("dataManagement"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                .background(colors.card)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = viewModel.t("backupRestoreSubtitle"), fontSize = 12.sp, color = colors.mutedForeground)
            
            val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                uri?.let {
                    if (com.bhojnify.android.utils.BackupManager.exportData(context, it, state)) {
                        Toast.makeText(context, viewModel.t("dataExportSuccess"), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, viewModel.t("dataExportFailed"), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let { fileUri ->
                    val importedState = com.bhojnify.android.utils.BackupManager.importData(context, fileUri)
                    if (importedState != null) {
                        viewModel.replaceState(importedState)
                        Toast.makeText(context, viewModel.t("dataImportSuccess"), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, viewModel.t("dataImportFailed"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.secondary)
                        .clickable { exportLauncher.launch("messmate_backup_${System.currentTimeMillis()}.json") }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Upload, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        Text(text = viewModel.t("exportBackup"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.secondary)
                        .clickable { importLauncher.launch(arrayOf("application/json", "*/*")) }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        Text(text = viewModel.t("importBackup"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    }
                }
            }
        }
    }
}
