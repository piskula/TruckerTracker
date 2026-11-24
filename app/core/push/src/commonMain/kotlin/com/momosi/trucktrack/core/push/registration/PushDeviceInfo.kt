package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.shared.push.PushPlatformDto

internal interface PushDeviceInfo {

    val platform: PushPlatformDto

    suspend fun deviceName(): String
}
