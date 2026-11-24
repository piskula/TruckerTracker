package com.momosi.trucktrack.feature.checklist.impl

import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType

sealed interface ChecklistAction {
    data object Retry : ChecklistAction
    data class TogglePicker(val type: VehicleType) : ChecklistAction
    data class SelectVehicle(val vehicle: Vehicle) : ChecklistAction
    data class Answer(val identifier: String, val result: CheckResult) : ChecklistAction
    data object Submit : ChecklistAction
    data object RequestClose : ChecklistAction
    data object ConfirmDiscard : ChecklistAction
    data object DismissDiscard : ChecklistAction
}
