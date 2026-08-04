package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonRole: ButtonRole = ButtonRole.Primary,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onSurface,
            )
        },
        text = {
            Text(
                text = message,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                text = confirmText,
                onClick = onConfirm,
                role = confirmButtonRole,
                modifier = Modifier.testTag("confirmation_dialog_confirm_button"),
            )
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.fillMaxWidth().testTag("confirmation_dialog_dismiss_button"),
                onClick = onDismiss,
            ) {
                Text(text = dismissText, style = AppTheme.typography.labelLarge, color = AppTheme.colors.onSurfaceVariant)
            }
        },
        containerColor = AppTheme.colors.surfaceContainerLowest,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun ConfirmationDialogPreview() {
    TruckTrackTheme {
        ConfirmationDialog(
            title = "Resolve Issue",
            message = "Are you sure you want to mark this issue as resolved?",
            confirmText = "Resolve",
            dismissText = "Cancel",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
