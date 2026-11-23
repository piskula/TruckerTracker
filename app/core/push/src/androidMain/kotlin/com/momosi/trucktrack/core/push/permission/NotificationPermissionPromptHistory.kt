package com.momosi.trucktrack.core.push.permission

import android.content.SharedPreferences
import androidx.core.content.edit

private const val SYSTEM_PROMPT_BLOCKED_KEY = "notification_permission_prompt_blocked"

internal class NotificationPermissionPromptHistory(private val preferences: SharedPreferences) {

    var isSystemPromptBlocked: Boolean
        get() = preferences.getBoolean(SYSTEM_PROMPT_BLOCKED_KEY, false)
        set(value) = preferences.edit { putBoolean(SYSTEM_PROMPT_BLOCKED_KEY, value) }
}
