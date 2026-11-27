package sk.momosilabs.truckTrack.checklist.model

enum class ChecklistState {
    NOT_REQUIRED,
    PENDING,
    COMPLETED,
}

data class ChecklistStatusModel(
    val state: ChecklistState,
    val response: ChecklistResponseModel?,
)
