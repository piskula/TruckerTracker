package com.momosi.trucktrack.core.push.permission

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

interface NotificationPermission {

    val currentStatus: StateFlow<NotificationPermissionStatus?>

    suspend fun status(): NotificationPermissionStatus

    fun openSettings()
}

@Composable
expect fun rememberNotificationPermissionRequester(onResult: () -> Unit): () -> Unit
