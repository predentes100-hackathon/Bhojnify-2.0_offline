package com.bhojnify.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.bhojnify.android.components.EmptyState
import com.bhojnify.android.components.Header
import com.bhojnify.android.components.SectionHeading
import com.bhojnify.android.navigation.Routes
import com.bhojnify.android.theme.BhojnifyTheme
import com.bhojnify.android.viewmodel.MessViewModel

@Composable
fun FeedbackScreen(navController: NavController, viewModel: MessViewModel) {
    val colors = BhojnifyTheme.colors
    val state by viewModel.state.collectAsState()
    val feedback = state.feedback

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
            title = viewModel.t("feedbackTitle"),
            subtitle = viewModel.t("feedbackSubtitle"),
            onActionClick = { navController.navigate(Routes.ADMIN) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .background(colors.primary)
                .padding(17.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Icon(Icons.Default.ChatBubbleOutline, null, tint = colors.accent, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = viewModel.t("notesRecorded", mapOf("count" to feedback.size, "plural" to if (feedback.size == 1) "" else "s")),
                    color = colors.primaryForeground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.t("improveMenu"),
                    color = colors.primaryForeground.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        SectionHeading(title = viewModel.t("recentNotes"))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .border(1.dp, colors.border, RoundedCornerShape(19.dp))
                .background(colors.card)
                .padding(horizontal = 15.dp)
        ) {
            if (feedback.isNotEmpty()) {
                feedback.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(item.rating) {
                                Icon(Icons.Default.Star, null, tint = colors.accent, modifier = Modifier.size(15.dp))
                            }
                        }
                        Text(
                            text = item.note.ifEmpty { viewModel.t("rated", mapOf("dish" to item.dish)) },
                            color = colors.foreground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "${item.dish} · ${item.date}", color = colors.mutedForeground, fontSize = 11.sp)
                    }
                }
            } else {
                EmptyState(
                    icon = Icons.Default.ChatBubbleOutline,
                    title = viewModel.t("noFeedback"),
                    detail = "Customer dish ratings and recommendations will appear here."
                )
            }
        }
    }
}