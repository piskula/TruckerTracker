package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.Shapes

class ErrorSnackbarHostState internal constructor(internal val hostState: SnackbarHostState, private val messages: ApiErrorMessages) {
    suspend fun showError(error: Throwable) {
        hostState.showSnackbar(message = error.toMessage(messages), duration = SnackbarDuration.Short)
    }
}

@Composable
fun rememberErrorSnackbarHostState(): ErrorSnackbarHostState {
    val hostState = remember { SnackbarHostState() }
    val messages = rememberApiErrorMessages()
    return remember(hostState, messages) { ErrorSnackbarHostState(hostState, messages) }
}

@Composable
fun ErrorSnackbarHost(state: ErrorSnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = state.hostState, modifier = modifier) { data ->
        Snackbar(
            modifier = Modifier.padding(12.dp),
            shape = Shapes.CardShape,
            containerColor = AppTheme.colors.errorContainer,
            contentColor = AppTheme.colors.onErrorContainer,
        ) {
            Text(text = data.visuals.message, color = AppTheme.colors.onErrorContainer)
        }
    }
}
