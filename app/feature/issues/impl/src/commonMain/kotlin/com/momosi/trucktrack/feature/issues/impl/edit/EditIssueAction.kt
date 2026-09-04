package com.momosi.trucktrack.feature.issues.impl.edit

import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.vehicle.model.Vehicle

sealed interface EditIssueAction {
    data class SelectVehicle(val vehicle: Vehicle) : EditIssueAction
    data object ToggleVehicleDropdown : EditIssueAction
    data class UpdateTitle(val title: String) : EditIssueAction
    data class UpdateDescription(val description: String) : EditIssueAction
    data class SelectPriority(val priority: IssuePriority) : EditIssueAction
    data object Submit : EditIssueAction
    data object Retry : EditIssueAction
}
