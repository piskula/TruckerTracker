package com.momosi.trucktrack.core.push.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch

@Composable
actual fun rememberNotificationPermissionRequester(onResult: () -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    return remember<() -> Unit>(scope) {
        {
            scope.launch {
                requestNotificationAuthorization()
                currentOnResult()
            }
        }
    }
}
