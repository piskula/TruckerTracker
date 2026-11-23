package com.momosi.trucktrack.core.push.permission

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.push.message.PUSH_NOTIFICATION_CHANNEL_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "NotificationPermission"

internal class AndroidNotificationPermission(private val context: Context, private val promptHistory: NotificationPermissionPromptHistory) : NotificationPermission {

    private val notificationManager = NotificationManagerCompat.from(context)

    override val currentStatus: StateFlow<NotificationPermissionStatus?>
        field = MutableStateFlow<NotificationPermissionStatus?>(null)

    override suspend fun status(): NotificationPermissionStatus = evaluateStatus().also { currentStatus.value = it }

    override fun openSettings() {
        val notificationSettings = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(notificationSettings)
        } catch (exception: ActivityNotFoundException) {
            Logger.w(TAG, exception, "App notification settings unavailable, opening app details")
            val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(appDetails)
        }
    }

    private fun evaluateStatus(): NotificationPermissionStatus {
        val notificationsDeliverable = notificationManager.areNotificationsEnabled() && !isChannelBlocked()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return if (notificationsDeliverable) NotificationPermissionStatus.Granted else NotificationPermissionStatus.Denied
        }

        val permissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (permissionGranted && promptHistory.isSystemPromptBlocked) promptHistory.isSystemPromptBlocked = false

        return when {
            permissionGranted && notificationsDeliverable -> NotificationPermissionStatus.Granted
            permissionGranted || promptHistory.isSystemPromptBlocked -> NotificationPermissionStatus.Denied
            else -> NotificationPermissionStatus.CanAsk
        }
    }

    private fun isChannelBlocked(): Boolean = notificationManager.getNotificationChannelCompat(PUSH_NOTIFICATION_CHANNEL_ID)?.importance == NotificationManagerCompat.IMPORTANCE_NONE
}
