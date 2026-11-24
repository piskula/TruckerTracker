package sk.momosilabs.truckTrack.api.checklist.dto

data class ChecklistResponseItemDto(
    val identifier: String,
    val texts: List<ChecklistTextDto>,
    val answer: Boolean,
)
