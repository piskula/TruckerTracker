package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun InfoDialog(
    title: String,
    dismissText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = AppTheme.typography.titleMedium, color = AppTheme.colors.onSurface)
        },
        text = {
            Column(content = content)
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.fillMaxWidth().testTag("info_dialog_dismiss_button"),
                text = dismissText,
                onClick = onDismiss,
            )
        },
        containerColor = AppTheme.colors.surfaceContainerLowest,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun InfoDialogPreview() {
    TruckTrackTheme {
        InfoDialog(
            title = "App Version",
            dismissText = "OK",
            onDismiss = {},
        ) {
            Text(text = "App: 1.4.2 (89)", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.onSurface)
            Text(text = "Server: 1.4.0 · Jul 20, 2026", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.onSurface)
        }
    }
}
