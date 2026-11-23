package sk.momosilabs.truckTrack.push.service.getCurrentUserPushDevice

import sk.momosilabs.truckTrack.push.model.PushDeviceModel

interface GetCurrentUserPushDeviceUseCase {

    fun get(): PushDeviceModel?
}
