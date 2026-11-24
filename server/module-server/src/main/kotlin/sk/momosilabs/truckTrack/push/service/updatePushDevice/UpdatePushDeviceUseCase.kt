package sk.momosilabs.truckTrack.push.service.updatePushDevice

interface UpdatePushDeviceUseCase {

    fun update(registeredFirebaseInstallationId: String, firebaseInstallationId: String)
}
