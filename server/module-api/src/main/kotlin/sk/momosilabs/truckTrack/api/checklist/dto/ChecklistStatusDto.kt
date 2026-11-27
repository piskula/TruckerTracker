package sk.momosilabs.truckTrack.api.checklist.dto

data class ChecklistStatusDto(
    val state: ChecklistStateDto,
    val response: ChecklistResponseDto?,
)
