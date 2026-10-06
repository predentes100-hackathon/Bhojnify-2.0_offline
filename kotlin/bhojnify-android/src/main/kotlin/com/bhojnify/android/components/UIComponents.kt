package com.bhojnify.android.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhojnify.android.theme.BhojnifyTheme

@Composable
fun Header(
    title: String,
    eyebrow: String? = null,
    subtitle: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = BhojnifyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    color = colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = title,
                color = colors.foreground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = colors.mutedForeground,
                    fontSize = 14.sp
                )
            }
        }
        if (onActionClick != null) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.card)
                    .border(1.dp, colors.border, CircleShape)
                    .clickable { onActionClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = colors.foreground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeading(
    title: String,
    action: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = BhojnifyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = colors.foreground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (action != null) {
            Text(
                text = action,
                color = colors.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onActionClick?.invoke() }
            )
        }
    }
}

enum class BadgeTone { GREEN, AMBER, RED, GRAY }

@Composable
fun Badge(
    label: String,
    tone: BadgeTone = BadgeTone.GREEN
) {
    val colors = BhojnifyTheme.colors
    val (bg, fg) = when (tone) {
        BadgeTone.GREEN -> colors.secondary to colors.primary
        BadgeTone.AMBER -> colors.accent to colors.accentForeground
        BadgeTone.RED -> colors.destructive to colors.destructiveForeground
        BadgeTone.GRAY -> colors.muted to colors.mutedForeground
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PrimaryButton(
    label: String,
    icon: ImageVector? = null,
    secondary: Boolean = false,
    disabled: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = BhojnifyTheme.colors
    val bg = if (secondary) colors.secondary else colors.primary
    val fg = if (secondary) colors.secondaryForeground else colors.primaryForeground
    val borderColor = if (secondary) colors.border else colors.primary

    Button(
        onClick = onClick,
        enabled = !disabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = bg.copy(alpha = 0.45f),
            disabledContentColor = fg.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = fg
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = fg
            )
        }
    }
}

@Composable
fun IconTile(
    icon: ImageVector,
    label: String,
    color: Color? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = BhojnifyTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color ?: colors.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (color != null) colors.card else colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.foreground,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    detail: String? = null,
    tone: BadgeTone = BadgeTone.GREEN,
    modifier: Modifier = Modifier
) {
    val colors = BhojnifyTheme.colors
    val (bg, fg) = when (tone) {
        BadgeTone.AMBER -> colors.accent to colors.accentForeground
        BadgeTone.RED -> colors.destructive to colors.destructiveForeground
        else -> colors.primary to colors.primaryForeground
    }

    Box(
        modifier = modifier
            .height(104.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = fg.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = fg,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            if (detail != null) {
                Text(
                    text = detail,
                    color = fg.copy(alpha = 0.82f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun RowItem(
    icon: ImageVector,
    title: String,
    detail: String? = null,
    rightContent: @Composable (() -> Unit)? = null,
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val colors = BhojnifyTheme.colors
    val iconBg = if (destructive) colors.destructive else colors.secondary
    val iconFg = if (destructive) colors.destructiveForeground else colors.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconFg,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.foreground
            )
            if (detail != null) {
                Text(
                    text = detail,
                    fontSize = 12.sp,
                    color = colors.mutedForeground,
                    lineHeight = 16.sp
                )
            }
        }
        if (rightContent != null) {
            rightContent()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.mutedForeground,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    val colors = BhojnifyTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.foreground
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = colors.mutedForeground, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.card,
                unfocusedContainerColor = colors.card,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.input,
                focusedTextColor = colors.foreground,
                unfocusedTextColor = colors.foreground
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    detail: String
) {
    val colors = BhojnifyTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.foreground
            )
            Text(
                text = detail,
                fontSize = 13.sp,
                color = colors.mutedForeground,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
