package com.momosi.trucktrack.core.push.registration

import android.content.SharedPreferences
import androidx.core.content.edit

internal const val PUSH_REGISTRATION_STORAGE = "push_registration"
private const val LAST_SENT_FIREBASE_INSTALLATION_ID_KEY = "last_sent_firebase_installation_id"

internal class AndroidPushRegistrationStorage(private val preferences: SharedPreferences) : PushRegistrationStorage {

    override var lastSentFirebaseInstallationId: String?
        get() = preferences.getString(LAST_SENT_FIREBASE_INSTALLATION_ID_KEY, null)
        set(value) = preferences.edit { putString(LAST_SENT_FIREBASE_INSTALLATION_ID_KEY, value) }
}
