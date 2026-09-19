package com.momosi.trucktrack.feature.issues.impl.detail.startworking

import com.momosi.trucktrack.core.issue.model.RepairType
import com.momosi.trucktrack.core.issue.model.VehicleSystem

sealed interface StartWorkingAction {
    data class SelectRepairType(val repairType: RepairType) : StartWorkingAction
    data class SelectVehicleSystem(val vehicleSystem: VehicleSystem) : StartWorkingAction
    data object Confirm : StartWorkingAction
}
