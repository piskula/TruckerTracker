package com.momosi.trucktrack.feature.issues.impl.edit

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.issue.model.EditingCapabilities
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.feature.issues.impl.SubmitStatus
import com.momosi.trucktrack.feature.issues.impl.VehiclesContent

@Immutable
data class EditIssueState(
    val loadState: EditIssueLoadState = EditIssueLoadState.Loading,
    val vehicles: VehiclesContent = VehiclesContent.Loading,
    val selectedVehicle: Vehicle? = null,
    val vehicleDropdownExpanded: Boolean = false,
    val title: String = "",
    val description: String = "",
    val selectedPriority: IssuePriority = IssuePriority.Medium,
    val submitStatus: SubmitStatus = SubmitStatus.Idle,
) {
    val editing: EditingCapabilities
        get() = (loadState as? EditIssueLoadState.Ready)?.editing ?: EditingCapabilities.None

    val titleError: Boolean
        get() = submitStatus != SubmitStatus.Idle && title.isBlank()

    val vehicleError: Boolean
        get() = submitStatus != SubmitStatus.Idle && selectedVehicle == null

    val isSubmitting: Boolean
        get() = submitStatus == SubmitStatus.InProgress

    val submissionFailed: Boolean
        get() = submitStatus == SubmitStatus.RequestFailed
}

@Immutable
sealed interface EditIssueLoadState {
    data object Loading : EditIssueLoadState
    data object Error : EditIssueLoadState

    @Immutable
    data class Ready(val editing: EditingCapabilities) : EditIssueLoadState
}
