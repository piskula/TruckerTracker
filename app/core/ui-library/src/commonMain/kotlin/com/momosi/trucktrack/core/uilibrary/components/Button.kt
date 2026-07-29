package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.TextButton as MaterialTextButton

enum class ButtonStyle { Filled, Tonal, Outlined, OutlinedFixed }

enum class ButtonRole { Primary, Warning, Positive, Open }

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    role: ButtonRole = ButtonRole.Primary,
    style: ButtonStyle = ButtonStyle.Filled,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
) {
    val (containerColor, contentColor) = when (style) {
        ButtonStyle.Filled -> when (role) {
            ButtonRole.Primary -> AppTheme.colors.primary to AppTheme.colors.onPrimary
            ButtonRole.Warning -> AppTheme.colors.warning to AppTheme.colors.onWarning
            ButtonRole.Positive -> AppTheme.colors.positive to AppTheme.colors.onPositive
            ButtonRole.Open -> AppTheme.colors.open to AppTheme.colors.onOpen
        }

        ButtonStyle.Tonal -> when (role) {
            ButtonRole.Primary -> AppTheme.colors.primaryContainer to AppTheme.colors.onPrimaryContainer
            ButtonRole.Warning -> AppTheme.colors.warningContainer to AppTheme.colors.onWarningContainer
            ButtonRole.Positive -> AppTheme.colors.positiveContainer to AppTheme.colors.onPositiveContainer
            ButtonRole.Open -> AppTheme.colors.openContainer to AppTheme.colors.onOpenContainer
        }

        ButtonStyle.Outlined -> Color.Transparent to AppTheme.colors.onSurface

        ButtonStyle.OutlinedFixed -> Color.Transparent to Color.White
    }
    val border = when (style) {
        ButtonStyle.Outlined -> BorderStroke(1.5.dp, AppTheme.colors.onSurface)
        ButtonStyle.OutlinedFixed -> BorderStroke(1.5.dp, Color.White)
        else -> null
    }

    MaterialButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 200.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor,
        ),
        border = border,
        contentPadding = contentPadding,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.dp,
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = text, style = AppTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MaterialTextButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(text = text, style = AppTheme.typography.labelLarge, color = AppTheme.colors.primary)
    }
}

@Preview
@Composable
private fun ButtonPreview() {
    TruckTrackTheme {
        Button(text = "Sign In", onClick = {})
    }
}

@Preview
@Composable
private fun ButtonLoadingPreview() {
    TruckTrackTheme {
        Button(text = "Sign In", onClick = {}, loading = true)
    }
}

@Preview
@Composable
private fun ButtonWarningPreview() {
    TruckTrackTheme {
        Button(
            text = "Start Working",
            onClick = {},
            role = ButtonRole.Warning,
            icon = TruckTrackIcons.Build,
        )
    }
}

@Preview
@Composable
private fun ButtonTonalWarningPreview() {
    TruckTrackTheme {
        Button(
            text = "Reassign to Me",
            onClick = {},
            role = ButtonRole.Warning,
            style = ButtonStyle.Tonal,
            icon = TruckTrackIcons.EmojiPeople,
        )
    }
}

@Preview
@Composable
private fun ButtonPositivePreview() {
    TruckTrackTheme {
        Button(
            text = "Resolve Issue",
            onClick = {},
            role = ButtonRole.Positive,
            icon = TruckTrackIcons.Check,
        )
    }
}

@Preview
@Composable
private fun ButtonOpenPreview() {
    TruckTrackTheme {
        Button(
            text = "Submit Issue",
            onClick = {},
            role = ButtonRole.Open,
        )
    }
}

@Preview
@Composable
private fun TextButtonPreview() {
    TruckTrackTheme {
        TextButton(text = "Reset", onClick = {})
    }
}
