package com.momosi.trucktrack.shared.push

import kotlinx.serialization.Serializable

@Serializable
data class PushDeviceRegistrationDto(
    val firebaseInstallationId: String,
    val deviceName: String,
    val platform: PushPlatformDto,
)
