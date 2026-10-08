package sk.momosilabs.truckTrack.issueManagement.service.updateIssue

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.config.GlobalForbiddenException
import sk.momosilabs.truckTrack.issueManagement.entity.IssuePriority
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.entity.RepairType
import sk.momosilabs.truckTrack.issueManagement.entity.VehicleSystem
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import sk.momosilabs.truckTrack.issueManagement.service.IssueListFilter
import sk.momosilabs.truckTrack.issueManagement.service.IssuePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.vehicle.entity.VehicleType
import sk.momosilabs.truckTrack.vehicle.model.VehicleModel
import sk.momosilabs.truckTrack.vehicle.service.VehiclePersistence
import java.time.OffsetDateTime
import java.util.UUID

class UpdateIssueTest {

    private val reporter = account("driver")
    private val mechanic = account("mechanic")
    private val otherDriver = account("other")
    private val truck = VehicleModel(id = 1, licensePlate = "BA-111-AA", make = "Volvo", model = "FH16", type = VehicleType.TRUCK)
    private val trailer = VehicleModel(id = 2, licensePlate = "BA-222-BB", make = "Krone", model = "Profi", type = VehicleType.TRAILER)

    @Test
    fun `reporter changing priority of an OPEN issue records a priority change`() {
        val persistence = FakeIssuePersistence(issue(IssueStatus.OPEN))

        useCase(persistence, reporter, isMechanic = false)
            .update(ISSUE_ID, command(priority = IssuePriority.P1_HIGH))

        assertThat(persistence.issue.priority).isEqualTo(IssuePriority.P1_HIGH)
        val history = persistence.history.single() as IssueHistoryModel.PriorityChange
        assertThat(history.priorityFrom).isEqualTo(IssuePriority.P5_LOW)
        assertThat(history.priorityTo).isEqualTo(IssuePriority.P1_HIGH)
        assertThat(history.performedBy).isEqualTo(reporter)
    }

    @Test
    fun `assigned mechanic can change priority of an IN_PROGRESS issue`() {
        val persistence = FakeIssuePersistence(issue(IssueStatus.IN_PROGRESS, assignedTo = mechanic))

        useCase(persistence, mechanic, isMechanic = true)
            .update(ISSUE_ID, command(priority = IssuePriority.P3_MEDIUM))

        assertThat(persistence.history.single()).isInstanceOf(IssueHistoryModel.PriorityChange::class.java)
    }

    @Test
    fun `a mechanic who is not assigned cannot change priority`() {
        val persistence = FakeIssuePersistence(issue(IssueStatus.OPEN))

        assertThatThrownBy {
            useCase(persistence, mechanic, isMechanic = true)
                .update(ISSUE_ID, command(priority = IssuePriority.P3_MEDIUM))
        }.isInstanceOf(GlobalForbiddenException::class.java)
        assertThat(persistence.history).isEmpty()
    }

    @Test
    fun `reporter cannot change priority once the issue is IN_PROGRESS`() {
        val persistence = FakeIssuePersistence(issue(IssueStatus.IN_PROGRESS, assignedTo = mechanic))

        assertThatThrownBy {
            useCase(persistence, reporter, isMechanic = false)
                .update(ISSUE_ID, command(priority = IssuePriority.P1_HIGH))
        }.isInstanceOf(GlobalForbiddenException::class.java)
        assertThat(persistence.history).isEmpty()
    }

    @Test
    fun `reporter cannot change description once the issue is IN_PROGRESS`() {
        val persistence = FakeIssuePersistence(issue(IssueStatus.IN_PROGRESS, assignedTo = mechanic))

        assertThatThrownBy {
            useCase(persistence, reporter, isMechanic = false)
                .update(ISSUE_ID, command(description = "New description"))
        }.isInstanceOf(GlobalForbiddenException::class.java)
        assertThat(persistence.history).isEmpty()
    }

    @Test
    fun `a driver who did not report the issue cannot change priority`() {
        val persistence = FakeIssuePersistence(issue(IssueStatus.OPEN))

        assertThatThrownBy {
            useCase(persistence, otherDriver, isMechanic = false)
                .update(ISSUE_ID, command(priority = IssuePriority.P1_HIGH))
        }.isInstanceOf(GlobalForbiddenException::class.java)
    }

    @Test
    fun `mixed edit records a separate entry per changed field`() {
        val persistence = FakeIssuePersistence(issue(IssueStatus.OPEN))

        useCase(persistence, reporter, isMechanic = false)
            .update(
                ISSUE_ID,
                command(title = "New title", description = "New description", priority = IssuePriority.P1_HIGH, vehicleId = trailer.id),
            )

        assertThat(persistence.history).hasSize(4)
        val titleChange = persistence.history.filterIsInstance<IssueHistoryModel.TitleChange>().single()
        assertThat(titleChange.titleFrom).isEqualTo(TITLE)
        assertThat(titleChange.titleTo).isEqualTo("New title")
        val descriptionChange = persistence.history.filterIsInstance<IssueHistoryModel.DescriptionChange>().single()
        assertThat(descriptionChange.descriptionFrom).isEqualTo(DESCRIPTION)
        assertThat(descriptionChange.descriptionTo).isEqualTo("New description")
        assertThat(persistence.history.filterIsInstance<IssueHistoryModel.PriorityChange>()).hasSize(1)
        val vehicleChange = persistence.history.filterIsInstance<IssueHistoryModel.VehicleChange>().single()
        assertThat(vehicleChange.vehicleFrom).isEqualTo(IssueHistoryModel.VehicleSnapshot(truck.id, truck.licensePlate))
        assertThat(vehicleChange.vehicleTo).isEqualTo(IssueHistoryModel.VehicleSnapshot(trailer.id, trailer.licensePlate))
    }

    @Test
    fun `assigned mechanic can change vehicle of an IN_PROGRESS issue`() {
        val persistence = FakeIssuePersistence(issue(IssueStatus.IN_PROGRESS, assignedTo = mechanic))

        useCase(persistence, mechanic, isMechanic = true)
            .update(ISSUE_ID, command(vehicleId = trailer.id))

        assertThat(persistence.history.single()).isInstanceOf(IssueHistoryModel.VehicleChange::class.java)
    }

    private fun useCase(persistence: IssuePersistence, currentUser: AccountModel, isMechanic: Boolean): UpdateIssue {
        val currentUserService = mock(CurrentUserService::class.java)
        `when`(currentUserService.currentUser()).thenReturn(currentUser)
        `when`(currentUserService.isMechanic()).thenReturn(isMechanic)
        return UpdateIssue(persistence, FakeVehiclePersistence(listOf(truck, trailer)), currentUserService)
    }

    private fun command(
        title: String = TITLE,
        description: String = DESCRIPTION,
        priority: IssuePriority = IssuePriority.P5_LOW,
        vehicleId: Long = truck.id,
    ) = UpdateIssueCommand(vehicleId = vehicleId, title = title, description = description, priority = priority)

    private fun issue(status: IssueStatus, assignedTo: AccountModel? = null) = IssueModel(
        id = ISSUE_ID,
        title = TITLE,
        description = DESCRIPTION,
        status = status,
        priority = IssuePriority.P5_LOW,
        vehicle = truck,
        reportedBy = reporter,
        assignedTo = assignedTo,
        repairType = null,
        vehicleSystem = null,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now(),
    )

    private fun account(username: String) = AccountModel(id = UUID.randomUUID(), username = username, firstName = username, lastName = "Test")

    private class FakeVehiclePersistence(private val vehicles: List<VehicleModel>) : VehiclePersistence {
        override fun findAll() = vehicles
        override fun findById(id: Long) = vehicles.single { it.id == id }
    }

    private class FakeIssuePersistence(var issue: IssueModel) : IssuePersistence {
        val history = mutableListOf<IssueHistoryModel>()

        override fun findByIdOrThrow(id: Long) = issue
        override fun update(model: IssueModel) = model.also { issue = it }
        override fun saveHistory(model: IssueHistoryModel) = model.also { history += it }

        override fun findPage(filter: IssueListFilter, pageable: Pageable): Page<IssueModel> = throw UnsupportedOperationException()
        override fun create(model: IssueModel): IssueModel = throw UnsupportedOperationException()
        override fun updateStatusAndAssignee(id: Long, status: IssueStatus, newAssignee: UUID?, updatedAt: OffsetDateTime): IssueModel =
            throw UnsupportedOperationException()
        override fun start(id: Long, mechanicId: UUID, repairType: RepairType, vehicleSystem: VehicleSystem, updatedAt: OffsetDateTime): IssueModel =
            throw UnsupportedOperationException()
        override fun findHistory(issueId: Long, pageable: Pageable): Page<IssueHistoryModel> = throw UnsupportedOperationException()
    }

    private companion object {
        const val ISSUE_ID = 42L
        const val TITLE = "Engine light"
        const val DESCRIPTION = "Warning light is on"
    }
}
