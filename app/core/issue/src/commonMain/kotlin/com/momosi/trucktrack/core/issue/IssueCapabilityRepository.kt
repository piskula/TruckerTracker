package com.momosi.trucktrack.core.issue

import com.momosi.trucktrack.core.issue.model.ActionCapabilities
import com.momosi.trucktrack.core.issue.model.EditingCapabilities
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssueCapabilities
import com.momosi.trucktrack.core.issue.model.IssueStateAction
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.user.model.User

interface IssueCapabilityRepository {
    fun resolve(issue: Issue, user: User): IssueCapabilities
}

class IssueCapabilityRepositoryImpl : IssueCapabilityRepository {
    override fun resolve(issue: Issue, user: User): IssueCapabilities = IssueCapabilities(
        editing = resolveEditing(issue, user),
        actions = resolveActions(issue, user),
    )

    private fun resolveEditing(issue: Issue, user: User): EditingCapabilities {
        if (issue.status == IssueStatus.Done || issue.status == IssueStatus.Cancelled) return EditingCapabilities.None

        val isReporter = isReportingDriver(issue, user)
        val isOpenOrInProgress = issue.status == IssueStatus.Open || issue.status == IssueStatus.InProgress
        val canEditAsReportingDriver = isReporter && issue.status == IssueStatus.Open
        val canEditWhileBeingWorkedOn = isReporter && isOpenOrInProgress

        return EditingCapabilities(
            canEditTitle = canEditAsReportingDriver,
            canEditDescription = canEditWhileBeingWorkedOn,
            canEditPriority = canEditWhileBeingWorkedOn,
            canEditVehicle = canEditAsReportingDriver || isAssignedMechanic(issue, user),
        )
    }

    private fun resolveActions(issue: Issue, user: User): ActionCapabilities {
        val canActAsReportingDriver = isReportingDriver(issue, user) && issue.status == IssueStatus.Open

        return ActionCapabilities(
            nextStateAction = resolveNextStateAction(issue, user, canActAsReportingDriver),
            canDeletePhotos = canActAsReportingDriver,
        )
    }

    private fun resolveNextStateAction(
        issue: Issue,
        user: User,
        canActAsReportingDriver: Boolean,
    ): IssueStateAction? {
        resolveMechanicStateAction(issue, user)?.let { return it }
        return if (canActAsReportingDriver) IssueStateAction.Cancel else null
    }

    private fun resolveMechanicStateAction(issue: Issue, user: User): IssueStateAction? {
        if (!user.isMechanic) return null
        return when {
            issue.status == IssueStatus.Open && issue.assignedTo?.id != user.id -> IssueStateAction.StartWorking
            issue.status == IssueStatus.InProgress && issue.assignedTo?.id == user.id -> IssueStateAction.ResolveIssue
            issue.status == IssueStatus.InProgress && issue.assignedTo?.id != user.id -> IssueStateAction.Reassign
            else -> null
        }
    }

    private fun isReportingDriver(issue: Issue, user: User) = user.isDriver && issue.reportedBy?.id == user.id

    private fun isAssignedMechanic(issue: Issue, user: User) = user.isMechanic && issue.assignedTo?.id == user.id
}
