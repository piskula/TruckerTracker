package com.momosi.trucktrack.shared.issue

import kotlinx.serialization.Serializable

@Serializable
enum class RepairTypeDto {
    DAMAGE,
    FAULT,
    INSTALLATION,
}
