package sk.momosilabs.truckTrack.api.checklist.dto

data class ChecklistResponseCreateDto(
    val truckId: Long,
    val trailerId: Long,
    val answers: List<ChecklistAnswerDto>,
)
