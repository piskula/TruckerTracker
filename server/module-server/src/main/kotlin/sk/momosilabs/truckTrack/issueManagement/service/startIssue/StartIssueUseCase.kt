package sk.momosilabs.truckTrack.issueManagement.service.startIssue

import sk.momosilabs.truckTrack.issueManagement.entity.RepairType
import sk.momosilabs.truckTrack.issueManagement.entity.VehicleSystem
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel

interface StartIssueUseCase {

    fun start(issueId: Long, repairType: RepairType, vehicleSystem: VehicleSystem): IssueModel
}
