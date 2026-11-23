package com.momosi.trucktrack.shared.push

import kotlinx.serialization.Serializable

@Serializable
data class PushDeviceUpdateDto(
    val firebaseInstallationId: String,
)
