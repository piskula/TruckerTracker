package com.momosi.trucktrack.core.push.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.app.ActivityCompat
import org.koin.compose.koinInject

@Composable
actual fun rememberNotificationPermissionRequester(onResult: () -> Unit): () -> Unit {
    val activity = LocalActivity.current
    val promptHistory = koinInject<NotificationPermissionPromptHistory>()
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val canAskAgain = activity != null &&
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
        promptHistory.isSystemPromptBlocked = !granted && !canAskAgain
        currentOnResult()
    }
    return remember(launcher) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                currentOnResult()
            }
        }
    }
}
