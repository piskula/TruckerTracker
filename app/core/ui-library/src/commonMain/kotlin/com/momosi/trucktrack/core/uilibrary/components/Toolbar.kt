package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Toolbar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceDim)
            .statusBarsPadding()
            .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
    ) {
        TopBarIconButton(
            icon = TruckTrackIcons.Back,
            onClick = onBack,
            modifier = Modifier.testTag("toolbar_back_button"),
        )
        Text(
            text = title,
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

@Preview
@Composable
private fun ToolbarSimplePreview() {
    TruckTrackTheme {
        Toolbar(
            title = "Profile",
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun ToolbarWithActionPreview() {
    TruckTrackTheme {
        Toolbar(
            title = "Issue Detail",
            onBack = {},
            actions = {
                TextButton(text = "Reset", onClick = {})
            },
        )
    }
}
