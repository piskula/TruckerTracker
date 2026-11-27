package com.momosi.trucktrack.core.checklist.model

sealed interface ChecklistStatus {
    data object Unknown : ChecklistStatus

    data object Unavailable : ChecklistStatus

    data object NotRequired : ChecklistStatus

    data object Pending : ChecklistStatus

    data class Completed(val response: ChecklistResponse) : ChecklistStatus
}
