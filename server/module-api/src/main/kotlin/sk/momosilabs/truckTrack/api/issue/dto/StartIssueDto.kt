package sk.momosilabs.truckTrack.api.issue.dto

data class StartIssueDto(
    val repairType: RepairTypeDto,
    val vehicleSystem: VehicleSystemDto,
)
