package com.momosi.trucktrack.core.push.registration

internal interface PushRegistrationStorage {
    var lastSentFirebaseInstallationId: String?
}
