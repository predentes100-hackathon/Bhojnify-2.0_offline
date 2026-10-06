package com.bhojnify.android.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bhojnify.android.components.FormField
import com.bhojnify.android.components.PrimaryButton
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import com.bhojnify.core.i18n.Language
import com.bhojnify.core.model.OwnerProfile

@Composable
fun OnboardingScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var messName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val finishSetup = {
        val n = name.trim()
        val m = messName.trim()
        val p = phone.trim()
        val l = location.trim()
        if (n.isEmpty() || m.isEmpty() || p.isEmpty() || l.isEmpty()) {
            Toast.makeText(context, viewModel.t("requiredProfileMessage"), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.completeOwnerSetup(
                OwnerProfile(
                    name = n,
                    messName = m,
                    phone = p,
                    email = email.trim(),
                    location = l
                )
            )
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.ONBOARDING) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top row: Brand Mark and Language Picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = viewModel.t("chooseLanguage"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.mutedForeground
                )
                Row(
                    modifier = Modifier
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .background(colors.card)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isEn = state.language == Language.EN
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isEn) colors.primary else Color.Transparent)
                            .clickable { viewModel.setLanguage(Language.EN) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = viewModel.t("english"),
                            fontSize = 12.sp,
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
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = viewModel.t("marathi"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMr) colors.primaryForeground else colors.foreground
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = viewModel.t("ownerSetup").uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = colors.primary
        )

        Text(
            text = viewModel.t("runMess"),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 37.sp,
            color = colors.foreground,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = viewModel.t("setupWorkspace"),
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = colors.mutedForeground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(23.dp))
                .background(colors.card, RoundedCornerShape(23.dp))
                .padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                text = viewModel.t("tellAboutYou"),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colors.foreground
            )
            Text(
                text = viewModel.t("requiredFields"),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = colors.mutedForeground,
                modifier = Modifier.offset(y = (-8).dp)
            )

            FormField(
                label = viewModel.t("ownerName"),
                value = name,
                onValueChange = { name = it },
                placeholder = viewModel.t("enterOwnerName")
            )
            FormField(
                label = viewModel.t("messName"),
                value = messName,
                onValueChange = { messName = it },
                placeholder = viewModel.t("enterMessName")
            )
            FormField(
                label = viewModel.t("phoneNumber"),
                value = phone,
                onValueChange = { phone = it },
                placeholder = viewModel.t("enterPhoneNumber"),
                keyboardType = KeyboardType.Phone
            )
            FormField(
                label = viewModel.t("cityLocation"),
                value = location,
                onValueChange = { location = it },
                placeholder = viewModel.t("enterCityLocation")
            )
            FormField(
                label = viewModel.t("emailOptional"),
                value = email,
                onValueChange = { email = it },
                placeholder = viewModel.t("enterEmail"),
                keyboardType = KeyboardType.Email
            )

            PrimaryButton(
                label = viewModel.t("createWorkspace"),
                icon = Icons.Default.ArrowForward,
                onClick = finishSetup
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = viewModel.t("localOnlySetup"),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.mutedForeground
            )
        }
    }
}