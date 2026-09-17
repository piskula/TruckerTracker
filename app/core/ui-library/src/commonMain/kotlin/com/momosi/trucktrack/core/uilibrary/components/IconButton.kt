package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme

enum class IconButtonStyle {
    Standard,
    StandardMuted,
    Filled,
    Outlined,
    Highlighted,
}

@Composable
fun IconButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IconButtonStyle = IconButtonStyle.Standard,
) {
    when (style) {
        IconButtonStyle.Standard -> {
            IconButton(onClick = onClick, modifier = modifier) {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        }

        IconButtonStyle.StandardMuted -> {
            IconButton(
                onClick = onClick,
                modifier = modifier,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = AppTheme.colors.onSurfaceVariant,
                ),
            ) {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        }

        IconButtonStyle.Filled -> {
            FilledIconButton(
                onClick = onClick,
                modifier = modifier,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = AppTheme.colors.surfaceVariant,
                    contentColor = AppTheme.colors.onSurface,
                ),
            ) {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        }

        IconButtonStyle.Outlined -> {
            OutlinedIconButton(onClick = onClick, modifier = modifier) {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        }

        IconButtonStyle.Highlighted -> {
            FilledIconButton(
                onClick = onClick,
                modifier = modifier,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = AppTheme.colors.primaryContainer,
                    contentColor = AppTheme.colors.onPrimaryContainer,
                ),
            ) {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IconButtonStandardPreview() {
    TruckTrackTheme {
        IconButton(imageVector = Icons.Filled.Star, onClick = {}, style = IconButtonStyle.Standard)
    }
}

@Preview(showBackground = true)
@Composable
private fun IconButtonStandardMutedPreview() {
    TruckTrackTheme {
        IconButton(imageVector = Icons.Filled.Star, onClick = {}, style = IconButtonStyle.StandardMuted)
    }
}

@Preview(showBackground = true)
@Composable
private fun IconButtonFilledPreview() {
    TruckTrackTheme {
        IconButton(imageVector = Icons.Filled.Star, onClick = {}, style = IconButtonStyle.Filled)
    }
}

@Preview(showBackground = true)
@Composable
private fun IconButtonOutlinedPreview() {
    TruckTrackTheme {
        IconButton(imageVector = Icons.Filled.Star, onClick = {}, style = IconButtonStyle.Outlined)
    }
}

@Preview(showBackground = true)
@Composable
private fun IconButtonHighlightedPreview() {
    TruckTrackTheme {
        IconButton(imageVector = Icons.Filled.Star, onClick = {}, style = IconButtonStyle.Highlighted)
    }
}
