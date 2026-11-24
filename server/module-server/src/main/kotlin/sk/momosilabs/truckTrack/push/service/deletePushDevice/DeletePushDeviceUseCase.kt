package sk.momosilabs.truckTrack.push.service.deletePushDevice

interface DeletePushDeviceUseCase {

    fun delete(firebaseInstallationId: String)
}
