package sk.momosilabs.truckTrack.push.service

data class PushDeviceReplacedEvent(
    val previousFirebaseInstallationId: String,
    val newDeviceName: String,
)
