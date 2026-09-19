package com.momosi.trucktrack.feature.issues.impl.detail.startworking

import com.momosi.trucktrack.core.issue.model.Issue

sealed interface StartWorkingEvent {
    data class Started(val issue: Issue) : StartWorkingEvent
}
