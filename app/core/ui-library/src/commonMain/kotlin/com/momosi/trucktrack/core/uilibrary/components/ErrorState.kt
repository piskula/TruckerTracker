package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.resources.Res
import com.momosi.trucktrack.core.uilibrary.resources.error_no_connection
import com.momosi.trucktrack.core.uilibrary.resources.retry
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorState(
    error: Throwable?,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryButtonModifier: Modifier = Modifier,
) {
    val isNoConnection = error is ApiException.NoConnection
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isNoConnection) TruckTrackIcons.NoConnection else TruckTrackIcons.Error,
                tint = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(56.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isNoConnection) stringResource(Res.string.error_no_connection) else message,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                text = stringResource(Res.string.retry),
                onClick = onRetry,
                modifier = retryButtonModifier,
            )
        }
    }
}

@Preview
@Composable
private fun ErrorStatePreview() {
    TruckTrackTheme {
        ErrorState(
            error = null,
            message = "Could not load issue details",
            onRetry = {},
        )
    }
}

@Preview
@Composable
private fun ErrorStateNoConnectionPreview() {
    TruckTrackTheme {
        ErrorState(
            error = ApiException.NoConnection,
            message = "Could not load issue details",
            onRetry = {},
        )
    }
}
