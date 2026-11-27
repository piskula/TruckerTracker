package sk.momosilabs.truckTrack.checklist.service.sendChecklistReminder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.checklist.model.ChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistReminderModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import sk.momosilabs.truckTrack.checklist.service.ChecklistPersistence
import sk.momosilabs.truckTrack.checklist.service.ChecklistReminderDueEvent
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class SendChecklistReminderTest {

    private val publishedEvents = mutableListOf<Any>()
    private val eventPublisher = ApplicationEventPublisher { publishedEvents += it }

    @Test
    fun `every due driver gets one logged reminder for the current week and stage, published for sending after commit`() {
        val persistence = FakeChecklistPersistence(dueDriverIds = listOf(FIRST_DRIVER_ID, SECOND_DRIVER_ID))

        val reminded = SendChecklistReminder(persistence, eventPublisher, WEDNESDAY_MORNING).send(ChecklistReminderStage.TWO_DAYS_LEFT)

        assertThat(reminded).isEqualTo(2)
        assertThat(persistence.queries).containsExactly(LocalDate.of(2026, 11, 23) to ChecklistReminderStage.TWO_DAYS_LEFT)
        assertThat(persistence.createdReminders).containsExactly(
            reminder(FIRST_DRIVER_ID),
            reminder(SECOND_DRIVER_ID),
        )
        assertThat(publishedEvents).containsExactly(
            ChecklistReminderDueEvent(accountId = FIRST_DRIVER_ID, stage = ChecklistReminderStage.TWO_DAYS_LEFT),
            ChecklistReminderDueEvent(accountId = SECOND_DRIVER_ID, stage = ChecklistReminderStage.TWO_DAYS_LEFT),
        )
    }

    @Test
    fun `no due driver logs and publishes nothing`() {
        val persistence = FakeChecklistPersistence(dueDriverIds = emptyList())

        val reminded = SendChecklistReminder(persistence, eventPublisher, WEDNESDAY_MORNING).send(ChecklistReminderStage.WEEK_START)

        assertThat(reminded).isZero()
        assertThat(persistence.createdReminders).isEmpty()
        assertThat(publishedEvents).isEmpty()
    }

    private fun reminder(accountId: UUID) = ChecklistReminderModel(
        accountId = accountId,
        periodStartDate = LocalDate.of(2026, 11, 23),
        stage = ChecklistReminderStage.TWO_DAYS_LEFT,
        sentAt = OffsetDateTime.now(WEDNESDAY_MORNING),
    )

    private class FakeChecklistPersistence(private val dueDriverIds: List<UUID>) : ChecklistPersistence {
        val queries = mutableListOf<Pair<LocalDate, ChecklistReminderStage>>()
        val createdReminders = mutableListOf<ChecklistReminderModel>()

        override fun findDriverIdsDueForReminder(periodStartDate: LocalDate, stage: ChecklistReminderStage): List<UUID> =
            dueDriverIds.also { queries += periodStartDate to stage }

        override fun createReminder(model: ChecklistReminderModel): ChecklistReminderModel =
            model.also { createdReminders += it }

        override fun findAllQuestions(): List<ChecklistQuestionModel> = throw UnsupportedOperationException()

        override fun lockAccount(accountId: UUID) = throw UnsupportedOperationException()

        override fun existsResponse(answeredById: UUID, periodStartDate: LocalDate): Boolean = throw UnsupportedOperationException()

        override fun createResponse(model: ChecklistResponseModel): ChecklistResponseModel = throw UnsupportedOperationException()
    }

    private companion object {
        val FIRST_DRIVER_ID: UUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val SECOND_DRIVER_ID: UUID = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val WEDNESDAY_MORNING: Clock = Clock.fixed(Instant.parse("2026-11-25T07:00:00Z"), ZoneOffset.UTC)
    }
}
