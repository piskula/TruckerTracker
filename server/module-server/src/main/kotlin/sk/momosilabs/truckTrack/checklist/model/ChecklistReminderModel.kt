package sk.momosilabs.truckTrack.checklist.model

import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class ChecklistReminderModel(
    val id: Long = 0L,
    val accountId: UUID,
    val periodStartDate: LocalDate,
    val stage: ChecklistReminderStage,
    val sentAt: OffsetDateTime,
)
