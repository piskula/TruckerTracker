package com.momosi.trucktrack.shared.push

import kotlinx.serialization.Serializable

@Serializable
enum class PushMessageTypeDto {
    DEVICE_UNREGISTERED,
    CHECKLIST_REMINDER,
}
