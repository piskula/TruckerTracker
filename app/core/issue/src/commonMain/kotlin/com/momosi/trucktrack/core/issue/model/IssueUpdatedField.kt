package com.momosi.trucktrack.core.issue.model

enum class IssueUpdatedField {
    Title,
    Description,
    Priority,
    Vehicle,
    ;

    companion object {
        fun fromApiValue(value: String): IssueUpdatedField = when (value) {
            "TITLE" -> Title
            "DESCRIPTION" -> Description
            "PRIORITY" -> Priority
            "VEHICLE" -> Vehicle
            else -> Title
        }
    }
}
