package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.Shapes
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme

@Immutable
sealed interface ChipVariant {
    data object Default : ChipVariant
    data object Tonal : ChipVariant
    data object Warning : ChipVariant
}

@Composable
fun Chip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    trailingIcon: ImageVector? = null,
    leadingIcon: ImageVector? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = AppTheme.typography.labelLarge,
            )
        },
        modifier = modifier,
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
        shape = Shapes.CardShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = AppTheme.colors.surfaceVariant,
            labelColor = AppTheme.colors.onSurfaceVariant,
            iconColor = AppTheme.colors.onSurfaceVariant,
            selectedContainerColor = AppTheme.colors.primaryContainer,
            selectedLabelColor = AppTheme.colors.onPrimaryContainer,
            selectedLeadingIconColor = AppTheme.colors.onPrimaryContainer,
            selectedTrailingIconColor = AppTheme.colors.onPrimaryContainer,
        ),
        border = null,
    )
}

@Composable
fun Chip(
    label: String,
    variant: ChipVariant,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val (containerColor, contentColor) = when (variant) {
        ChipVariant.Default -> AppTheme.colors.surfaceVariant to AppTheme.colors.onSurfaceVariant
        ChipVariant.Tonal -> AppTheme.colors.primaryContainer to AppTheme.colors.onPrimaryContainer
        ChipVariant.Warning -> AppTheme.colors.warningContainer to AppTheme.colors.warning
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .background(color = containerColor, shape = Shapes.CardShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = label,
            style = AppTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Composable
fun OutlinedChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    trailingIcon: ImageVector? = null,
    leadingIcon: ImageVector? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = AppTheme.typography.labelLarge,
            )
        },
        modifier = modifier,
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
        shape = Shapes.CardShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = AppTheme.colors.onSurfaceVariant,
            iconColor = AppTheme.colors.onSurfaceVariant,
            selectedContainerColor = AppTheme.colors.primaryContainer,
            selectedLabelColor = AppTheme.colors.onPrimaryContainer,
            selectedLeadingIconColor = AppTheme.colors.onPrimaryContainer,
            selectedTrailingIconColor = AppTheme.colors.onPrimaryContainer,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
        ),
    )
}

@Preview
@Composable
private fun ChipSurfacePreview() {
    TruckTrackTheme {
        Chip(label = "Size", variant = ChipVariant.Default)
    }
}

@Preview
@Composable
private fun ChipTonalPreview() {
    TruckTrackTheme {
        Chip(label = "v2.1.0 (210)", variant = ChipVariant.Tonal)
    }
}

@Preview
@Composable
private fun ChipWarningPreview() {
    TruckTrackTheme {
        Chip(label = "SDK 28", variant = ChipVariant.Warning)
    }
}

@Preview
@Composable
private fun ChipClickableUnselectedPreview() {
    TruckTrackTheme {
        Chip(label = "Size", onClick = {})
    }
}

@Preview
@Composable
private fun ChipClickableSelectedPreview() {
    TruckTrackTheme {
        Chip(
            label = "Name",
            onClick = {},
            selected = true,
            trailingIcon = TruckTrackIcons.SortAscending,
        )
    }
}

@Preview
@Composable
private fun OutlinedChipUnselectedPreview() {
    TruckTrackTheme {
        OutlinedChip(
            label = "Match: Any",
            onClick = {},
            trailingIcon = TruckTrackIcons.ArrowDropDown,
        )
    }
}

@Preview
@Composable
private fun OutlinedChipSelectedPreview() {
    TruckTrackTheme {
        OutlinedChip(
            label = "Match: All",
            onClick = {},
            selected = true,
            trailingIcon = TruckTrackIcons.ArrowDropDown,
        )
    }
}
