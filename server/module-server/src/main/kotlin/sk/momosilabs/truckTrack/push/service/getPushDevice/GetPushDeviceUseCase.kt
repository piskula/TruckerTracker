package sk.momosilabs.truckTrack.push.service.getPushDevice

import sk.momosilabs.truckTrack.push.model.PushDeviceModel

interface GetPushDeviceUseCase {

    fun get(): PushDeviceModel?
}
