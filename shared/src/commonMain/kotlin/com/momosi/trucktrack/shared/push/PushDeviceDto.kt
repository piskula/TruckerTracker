package com.momosi.trucktrack.shared.push

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PushDeviceDto(
    val firebaseInstallationId: String,
    val deviceName: String,
    val platform: PushPlatformDto,
    val registeredAt: Instant,
)
