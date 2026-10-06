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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bhojnify.android.components.*
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel
import com.bhojnify.core.model.Meal
import com.bhojnify.core.service.DateUtils
import java.time.LocalDate

@Composable
fun MenuScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val menus = state.menus
    val todayStr = LocalDate.now().toString()
    val tomorrowStr = DateUtils.addDays(todayStr, 1)

    var selectedDate by remember { mutableStateOf(todayStr) }
    var selectedMeal by remember { mutableStateOf(Meal.LUNCH) }
    var dish by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val mealSlots = listOf(Meal.BREAKFAST, Meal.LUNCH, Meal.DINNER)

    val add = {
        if (dish.trim().isEmpty() || selectedDate.trim().isEmpty()) {
            Toast.makeText(context, viewModel.t("dishNameMessage"), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addMenu(
                day = selectedDate.trim(),
                meal = selectedMeal,
                dish = dish.trim(),
                note = note.trim().ifEmpty { viewModel.t("freshlyPrepared") }
            )
            dish = ""
            note = ""
            Toast.makeText(context, "Dish added to menu!", Toast.LENGTH_SHORT).show()
        }
    }

    // Group menus by date
    val groupedMenus = menus.groupBy { it.day }.toSortedMap()

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
            title = viewModel.t("menuPlanner"),
            subtitle = viewModel.t("shapeMeals"),
            onActionClick = { navController.navigate(Routes.PROFILE) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.primary)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.CalendarToday, null, tint = colors.accent, modifier = Modifier.size(26.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = viewModel.t("nextUp"), color = colors.primaryForeground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(text = viewModel.t("planAhead"), color = colors.primaryForeground.copy(alpha = 0.76f), fontSize = 12.sp)
            }
        }

        SectionHeading(title = viewModel.t("publishedMenu"))

        if (groupedMenus.isNotEmpty()) {
            groupedMenus.forEach { (dateKey, items) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Event, null, tint = colors.primary, modifier = Modifier.size(15.dp))
                        Text(
                            text = if (dateKey == todayStr) "${viewModel.t("today")} ($dateKey)" else if (dateKey == tomorrowStr) "${viewModel.t("tomorrow")} ($dateKey)" else dateKey,
                            color = colors.foreground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Badge(label = "${items.size} ${viewModel.t("menu").lowercase()}", tone = BadgeTone.GRAY)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(19.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                            .background(colors.card)
                            .padding(horizontal = 14.dp)
                    ) {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(11.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp, 38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = viewModel.localized(item.meal.name).take(4),
                                        color = colors.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = viewModel.localized(item.meal.name), color = colors.mutedForeground, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Badge(label = viewModel.t("published"))
                                    }
                                    Text(text = item.dish, color = colors.foreground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    if (item.note.isNotEmpty()) {
                                        Text(text = item.note, color = colors.mutedForeground, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            EmptyState(
                icon = Icons.Default.RestaurantMenu,
                title = viewModel.t("noMenuItems"),
                detail = "Create breakfast, lunch, and dinner plans for the upcoming week."
            )
        }

        SectionHeading(title = viewModel.t("addToPlan"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .background(colors.card)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(text = viewModel.t("date"), color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(1.dp, if (selectedDate == todayStr) colors.primary else colors.border, RoundedCornerShape(11.dp))
                        .background(if (selectedDate == todayStr) colors.secondary else colors.background)
                        .clickable { selectedDate = todayStr },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = viewModel.t("today"), color = if (selectedDate == todayStr) colors.primary else colors.foreground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(1.dp, if (selectedDate == tomorrowStr) colors.primary else colors.border, RoundedCornerShape(11.dp))
                        .background(if (selectedDate == tomorrowStr) colors.secondary else colors.background)
                        .clickable { selectedDate = tomorrowStr },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = viewModel.t("tomorrow"), color = if (selectedDate == tomorrowStr) colors.primary else colors.foreground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            FormField(label = "", value = selectedDate, onValueChange = { selectedDate = it }, placeholder = "YYYY-MM-DD")

            Text(text = viewModel.t("mealSlot"), color = colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                mealSlots.forEach { slot ->
                    val isSelected = selectedMeal == slot
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.secondary else colors.background)
                            .clickable { selectedMeal = slot },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.localized(slot.name),
                            color = if (isSelected) colors.primary else colors.foreground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            FormField(label = viewModel.t("dish"), value = dish, onValueChange = { dish = it }, placeholder = viewModel.t("enterDishName"))
            FormField(label = viewModel.t("notes"), value = note, onValueChange = { note = it }, placeholder = viewModel.t("enterMenuNotes"))

            PrimaryButton(label = viewModel.t("publishMenuItem"), icon = Icons.Default.Check, onClick = add)
        }
    }
}