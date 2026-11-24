package com.momosi.trucktrack.feature.issues.impl.create

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.common.io.PhotoData
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.uilibrary.vehicle.VehiclesContent
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.feature.issues.impl.SubmitStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CreateIssueState(
    val vehicles: VehiclesContent = VehiclesContent.Loading,
    val selectedVehicle: Vehicle? = null,
    val vehicleDropdownExpanded: Boolean = false,
    val title: String = "",
    val description: String = "",
    val selectedPriority: IssuePriority = IssuePriority.Medium,
    val photos: ImmutableList<PhotoData> = persistentListOf(),
    val submitStatus: SubmitStatus = SubmitStatus.Idle,
) {
    val titleError: Boolean
        get() = submitStatus != SubmitStatus.Idle && title.isBlank()

    val vehicleError: Boolean
        get() = submitStatus != SubmitStatus.Idle && selectedVehicle == null

    val isSubmitting: Boolean
        get() = submitStatus == SubmitStatus.InProgress

    val submissionFailed: Boolean
        get() = submitStatus == SubmitStatus.RequestFailed
}
