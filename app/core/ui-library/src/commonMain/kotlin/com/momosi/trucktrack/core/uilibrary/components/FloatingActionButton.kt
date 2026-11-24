package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import androidx.compose.material3.FloatingActionButton as MaterialFab

@Composable
fun FloatingActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MaterialFab(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = AppTheme.colors.primary,
        contentColor = AppTheme.colors.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}

@Composable
fun SmallExtendedFloatingActionButton(
    text: String,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(40.dp)
            .clip(SmallFabShape)
            .background(AppTheme.colors.primaryContainer, SmallFabShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (expanded) null else text,
            tint = AppTheme.colors.onPrimaryContainer,
            modifier = Modifier.size(24.dp),
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
        ) {
            Text(
                text = text,
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colors.onPrimaryContainer,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
            )
        }
    }
}

private val SmallFabShape = RoundedCornerShape(12.dp)

@Preview
@Composable
private fun FloatingActionButtonPreview() {
    TruckTrackTheme {
        FloatingActionButton(icon = TruckTrackIcons.Add, onClick = {})
    }
}

@Preview
@Composable
private fun SmallExtendedFloatingActionButtonExpandedPreview() {
    TruckTrackTheme {
        SmallExtendedFloatingActionButton(text = "Checklist", icon = TruckTrackIcons.Checklist, expanded = true, onClick = {})
    }
}

@Preview
@Composable
private fun SmallExtendedFloatingActionButtonCollapsedPreview() {
    TruckTrackTheme {
        SmallExtendedFloatingActionButton(text = "Checklist", icon = TruckTrackIcons.Checklist, expanded = false, onClick = {})
    }
}
