package com.momosi.trucktrack.core.issue.model

enum class IssueStatus {
    Open,
    InProgress,
    Done,
    Cancelled,
    ;

    companion object {
        fun fromApiValue(value: String): IssueStatus = when (value) {
            "OPEN" -> Open
            "IN_PROGRESS" -> InProgress
            "DONE" -> Done
            "CANCELLED" -> Cancelled
            else -> Open
        }
    }

    fun toApiValue(): String = when (this) {
        Open -> "OPEN"
        InProgress -> "IN_PROGRESS"
        Done -> "DONE"
        Cancelled -> "CANCELLED"
    }
}
