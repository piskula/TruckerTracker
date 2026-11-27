package sk.momosilabs.truckTrack.checklist.service

import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import java.util.UUID

data class ChecklistReminderDueEvent(
    val accountId: UUID,
    val stage: ChecklistReminderStage,
)
