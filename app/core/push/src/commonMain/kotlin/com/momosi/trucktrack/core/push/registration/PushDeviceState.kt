package com.momosi.trucktrack.core.push.registration

sealed interface PushDeviceState {
    data object Unknown : PushDeviceState
    data object ThisDevice : PushDeviceState
    data class OtherDevice(val deviceName: String) : PushDeviceState
    data object None : PushDeviceState
}
