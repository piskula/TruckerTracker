package sk.momosilabs.truckTrack.issueManagement.persistence.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.issueManagement.entity.IssueEntity
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEntity
import sk.momosilabs.truckTrack.issueManagement.entity.IssuePriority
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime
import sk.momosilabs.truckTrack.vehicle.entity.VehicleEntity
import sk.momosilabs.truckTrack.vehicle.entity.VehicleType
import tools.jackson.databind.ObjectMapper
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

@JsonTest
class IssueHistoryDetailsJsonTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val performer = AccountModel(id = UUID.randomUUID(), username = "mech", firstName = "Mattia", lastName = "Binotto")
    private val previousAssignee = AccountModel(id = UUID.randomUUID(), username = "prev", firstName = "Toto", lastName = "Wolff")
    private val createdAt =OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)

    @Test
    fun `every history type survives a round trip through the details column`() {
        val models = listOf(
            IssueHistoryModel.Comment(UUID.randomUUID(), ISSUE_ID, performer, createdAt, "Parts ordered"),
            IssueHistoryModel.TitleChange(UUID.randomUUID(), ISSUE_ID, performer, createdAt, "Old title", "New title"),
            IssueHistoryModel.DescriptionChange(UUID.randomUUID(), ISSUE_ID, performer, createdAt, "Old \"quoted\" text", "New\nmultiline"),
            IssueHistoryModel.PriorityChange(UUID.randomUUID(), ISSUE_ID, performer, createdAt, IssuePriority.P5_LOW, IssuePriority.P1_HIGH),
            IssueHistoryModel.VehicleChange(
                UUID.randomUUID(), ISSUE_ID, performer, createdAt,
                IssueHistoryModel.VehicleSnapshot(1, "BA-111-AA"),
                IssueHistoryModel.VehicleSnapshot(2, "BA-222-BB"),
            ),
            IssueHistoryModel.AssigneeChange(UUID.randomUUID(), ISSUE_ID, performer, createdAt, assigneeFrom = previousAssignee, assigneeTo = performer),
            IssueHistoryModel.StatusChange(UUID.randomUUID(), ISSUE_ID, performer, createdAt, IssueStatus.OPEN, IssueStatus.IN_PROGRESS),
        )

        models.forEach { model ->
            assertThat(model.toPersistedEntity().toModel(objectMapper)).isEqualTo(model)
        }
    }

    @Test
    fun `migrated rows written by the SQL changeset are readable`() {
        val comment = entity(IssueHistoryModel.Comment(UUID.randomUUID(), ISSUE_ID, performer, createdAt, "x"), """{"commentText": "Legacy"}""")
        val assignee = entity(
            IssueHistoryModel.AssigneeChange(UUID.randomUUID(), ISSUE_ID, performer, createdAt, previousAssignee, performer),
            """
            {
              "assigneeFrom": {"id": "${previousAssignee.id}", "username": "${previousAssignee.username}", "firstName": "${previousAssignee.firstName}", "lastName": "${previousAssignee.lastName}"},
              "assigneeTo": {"id": "${performer.id}", "username": "${performer.username}", "firstName": "${performer.firstName}", "lastName": "${performer.lastName}"}
            }
            """,
        )

        assertThat((comment.toModel(objectMapper) as IssueHistoryModel.Comment).commentText).isEqualTo("Legacy")
        val migratedAssignee = assignee.toModel(objectMapper) as IssueHistoryModel.AssigneeChange
        assertThat(migratedAssignee.assigneeFrom).isEqualTo(previousAssignee)
        assertThat(migratedAssignee.assigneeTo).isEqualTo(performer)
    }

    private fun IssueHistoryModel.toPersistedEntity() = entity(this, toDetailsJson(objectMapper))

    private fun entity(model: IssueHistoryModel, details: String?): IssueHistoryEntity {
        val account = AccountEntity(id = performer.id, username = performer.username, firstName = performer.firstName, lastName = performer.lastName)
        val issue = IssueEntity(
            id = ISSUE_ID,
            title = "t",
            description = "d",
            status = IssueStatus.OPEN,
            priority = IssuePriority.P3_MEDIUM,
            vehicle = VehicleEntity(id = 1, licensePlate = "BA-111-AA", make = "Volvo", model = "FH16", type = VehicleType.TRUCK),
            reportedBy = account,
            assignedTo = null,
            repairType = null,
            vehicleSystem = null,
            createdAtUtc = createdAt.toUtcLocalDateTime(),
            updatedAtUtc = createdAt.toUtcLocalDateTime(),
        )
        return IssueHistoryEntity(
            id = model.id,
            issue = issue,
            type = model.toEventType(),
            performedBy = account,
            createdAtUtc = model.createdAt.toUtcLocalDateTime(),
            statusFrom = (model as? IssueHistoryModel.StatusChange)?.statusFrom,
            statusTo = (model as? IssueHistoryModel.StatusChange)?.statusTo,
            details = details,
        )
    }

    private companion object {
        const val ISSUE_ID = 7L
    }
}
