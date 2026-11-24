package com.momosi.trucktrack.core.push.firebase

import android.content.SharedPreferences
import androidx.core.content.edit

private const val LAST_RECEIVED_FIREBASE_INSTALLATION_ID_KEY = "last_received_firebase_installation_id"

internal class ReceivedFirebaseInstallationIdStorage(private val preferences: SharedPreferences) {

    var lastReceivedFirebaseInstallationId: String?
        get() = preferences.getString(LAST_RECEIVED_FIREBASE_INSTALLATION_ID_KEY, null)
        set(value) = preferences.edit { putString(LAST_RECEIVED_FIREBASE_INSTALLATION_ID_KEY, value) }
}
