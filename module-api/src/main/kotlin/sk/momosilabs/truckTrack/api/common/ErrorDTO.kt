package sk.momosilabs.truckTrack.api.common

import java.util.UUID

data class ErrorDTO(
    val userMessage: String,
    val errorIdentifier: UUID,
)
