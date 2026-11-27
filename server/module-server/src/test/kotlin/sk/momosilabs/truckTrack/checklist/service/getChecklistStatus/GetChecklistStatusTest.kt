package sk.momosilabs.truckTrack.checklist.service.getChecklistStatus

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.checklist.model.ChecklistPeriod
import sk.momosilabs.truckTrack.checklist.model.DailyChecklistPeriod
import sk.momosilabs.truckTrack.checklist.model.ChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistReminderModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseAnswers
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistState
import sk.momosilabs.truckTrack.checklist.model.VehicleSnapshot
import sk.momosilabs.truckTrack.checklist.model.WeeklyChecklistPeriod
import sk.momosilabs.truckTrack.checklist.service.ChecklistPersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class GetChecklistStatusTest {

    @Test
    fun `a driver without a response this week has a pending checklist`() {
        val status = useCase(FakeChecklistPersistence(), isDriver = true, isMechanic = false).get()

        assertThat(status.state).isEqualTo(ChecklistState.PENDING)
        assertThat(status.response).isNull()
    }

    @Test
    fun `a response this week completes the checklist and is returned`() {
        val response = responseFor(LocalDate.of(2026, 11, 23))

        val status = useCase(FakeChecklistPersistence(listOf(response)), isDriver = true, isMechanic = false).get()

        assertThat(status.state).isEqualTo(ChecklistState.COMPLETED)
        assertThat(status.response).isEqualTo(response)
    }

    @Test
    fun `mechanics and dual role users are not required to fill the checklist, matching the reminder rule`() {
        val mechanic = useCase(FakeChecklistPersistence(), isDriver = false, isMechanic = true).get()
        val dualRole = useCase(FakeChecklistPersistence(), isDriver = true, isMechanic = true).get()

        assertThat(mechanic.state).isEqualTo(ChecklistState.NOT_REQUIRED)
        assertThat(dualRole.state).isEqualTo(ChecklistState.NOT_REQUIRED)
    }

    @Test
    fun `a user who is not required but still submitted sees the completed checklist`() {
        val response = responseFor(LocalDate.of(2026, 11, 23))

        val status = useCase(FakeChecklistPersistence(listOf(response)), isDriver = true, isMechanic = true).get()

        assertThat(status.state).isEqualTo(ChecklistState.COMPLETED)
    }

    @Test
    fun `the current week follows Bratislava time, so last week's response no longer counts on Monday morning`() {
        val persistence = FakeChecklistPersistence(listOf(responseFor(LocalDate.of(2026, 11, 23))))

        val sundayEvening = useCase(persistence, isDriver = true, isMechanic = false, clock = clockAt("2026-11-29T22:59:00Z")).get()
        val mondayMidnight = useCase(persistence, isDriver = true, isMechanic = false, clock = clockAt("2026-11-29T23:00:00Z")).get()

        assertThat(sundayEvening.state).isEqualTo(ChecklistState.COMPLETED)
        assertThat(mondayMidnight.state).isEqualTo(ChecklistState.PENDING)
    }

    @Test
    fun `daily completion expires at Bratislava midnight even within the same week`() {
        val response = responseFor(LocalDate.of(2026, 11, 27))
        val persistence = FakeChecklistPersistence(listOf(response))
        val beforeMidnight = useCase(
            persistence, isDriver = true, isMechanic = false, clock = clockAt("2026-11-27T22:59:00Z"), checklistPeriod = DailyChecklistPeriod,
        ).get()
        val afterMidnight = useCase(
            persistence, isDriver = true, isMechanic = false, clock = clockAt("2026-11-27T23:00:00Z"), checklistPeriod = DailyChecklistPeriod,
        ).get()

        assertThat(beforeMidnight.state).isEqualTo(ChecklistState.COMPLETED)
        assertThat(beforeMidnight.response).isEqualTo(response)
        assertThat(afterMidnight.state).isEqualTo(ChecklistState.PENDING)
        assertThat(afterMidnight.response).isNull()
    }

    private fun useCase(
        persistence: ChecklistPersistence,
        isDriver: Boolean,
        isMechanic: Boolean,
        clock: Clock = FIXED_CLOCK,
        checklistPeriod: ChecklistPeriod = WeeklyChecklistPeriod,
    ): GetChecklistStatus {
        val currentUserService = mock(CurrentUserService::class.java)
        `when`(currentUserService.currentUserId()).thenReturn(USER_ID)
        `when`(currentUserService.isDriver()).thenReturn(isDriver)
        `when`(currentUserService.isMechanic()).thenReturn(isMechanic)
        return GetChecklistStatus(persistence, currentUserService, clock, checklistPeriod)
    }

    private fun clockAt(instant: String): Clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC)

    private fun responseFor(periodStartDate: LocalDate) = ChecklistResponseModel(
        id = 7,
        truckId = 1,
        trailerId = 2,
        answeredById = USER_ID,
        answeredAt = OffsetDateTime.of(2026, 11, 24, 9, 30, 0, 0, ZoneOffset.UTC),
        periodStartDate = periodStartDate,
        answers = ChecklistResponseAnswers(
            truck = VehicleSnapshot(id = 1, licensePlate = "BA-111-AA"),
            trailer = VehicleSnapshot(id = 2, licensePlate = "BA-222-BB"),
            items = emptyList(),
        ),
    )

    private class FakeChecklistPersistence(private val responses: List<ChecklistResponseModel> = emptyList()) : ChecklistPersistence {
        override fun findAllQuestions(): List<ChecklistQuestionModel> = throw UnsupportedOperationException()

        override fun lockAccount(accountId: UUID) = throw UnsupportedOperationException()

        override fun existsResponse(answeredById: UUID, periodStartDate: LocalDate): Boolean = throw UnsupportedOperationException()

        override fun findResponse(answeredById: UUID, periodStartDate: LocalDate): ChecklistResponseModel? =
            responses.find { it.answeredById == answeredById && it.periodStartDate == periodStartDate }

        override fun createResponse(model: ChecklistResponseModel): ChecklistResponseModel = throw UnsupportedOperationException()

        override fun findDriverIdsDueForReminder(periodStartDate: LocalDate, stage: ChecklistReminderStage): List<UUID> =
            throw UnsupportedOperationException()

        override fun createReminder(model: ChecklistReminderModel): ChecklistReminderModel = throw UnsupportedOperationException()
    }

    private companion object {
        val USER_ID: UUID = UUID.fromString("11111111-2222-3333-4444-555555555555")
        val FIXED_CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-25T10:00:00Z"), ZoneOffset.UTC)
    }
}
