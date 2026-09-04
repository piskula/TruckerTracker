package com.momosi.trucktrack.core.issue.model

data class IssueUpdate(val vehicleId: Long, val title: String, val description: String, val priority: IssuePriority)
