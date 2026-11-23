package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.core.push.firebase.IosPushEvents
import com.momosi.trucktrack.shared.push.PushPlatformDto
import platform.UIKit.UIDevice

internal class IosPushDeviceInfo : PushDeviceInfo {

    override val platform: PushPlatformDto = PushPlatformDto.IOS

    override suspend fun deviceName(): String = IosPushEvents.deviceName?.takeIf { it.isNotBlank() } ?: UIDevice.currentDevice.model
}
