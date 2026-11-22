package com.momosi.trucktrack.feature.issues.impl.create

import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import io.github.vinceglb.filekit.PlatformFile

sealed interface CreateIssueAction {
    data class SelectVehicle(val vehicle: Vehicle) : CreateIssueAction
    data class UpdateTitle(val title: String) : CreateIssueAction
    data class UpdateDescription(val description: String) : CreateIssueAction
    data class SelectPriority(val priority: IssuePriority) : CreateIssueAction
    data class AddPhotos(val files: List<PlatformFile>) : CreateIssueAction
    data class PhotoPickerFailed(val cause: Throwable) : CreateIssueAction
    data class RemovePhoto(val fileName: String) : CreateIssueAction
    data object Submit : CreateIssueAction
    data object ToggleVehicleDropdown : CreateIssueAction
}
