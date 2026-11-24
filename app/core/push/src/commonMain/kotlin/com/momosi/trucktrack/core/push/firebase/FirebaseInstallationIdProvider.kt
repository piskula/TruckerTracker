package com.momosi.trucktrack.core.push.firebase

import kotlinx.coroutines.flow.Flow

interface FirebaseInstallationIdProvider {

    val firebaseInstallationIdChanges: Flow<String>

    suspend fun currentFirebaseInstallationId(): String?
}
