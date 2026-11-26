package sk.momosilabs.truckTrack.checklist.service.sendChecklistReminder

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.checklist.model.ChecklistPeriod
import sk.momosilabs.truckTrack.checklist.model.ChecklistReminderModel
import sk.momosilabs.truckTrack.checklist.service.ChecklistPersistence
import sk.momosilabs.truckTrack.checklist.service.ChecklistReminderDueEvent
import java.time.Clock
import java.time.OffsetDateTime

@Service
class SendChecklistReminder(
    private val checklistPersistence: ChecklistPersistence,
    private val eventPublisher: ApplicationEventPublisher,
    private val clock: Clock,
) : SendChecklistReminderUseCase {

    @Transactional
    override fun send(stage: ChecklistReminderStage): Int {
        val now = OffsetDateTime.now(clock)
        val periodStartDate = ChecklistPeriod.startDateOf(now)
        val driverIds = checklistPersistence.findDriverIdsDueForReminder(periodStartDate, stage)
        driverIds.forEach { driverId ->
            checklistPersistence.createReminder(
                ChecklistReminderModel(
                    accountId = driverId,
                    periodStartDate = periodStartDate,
                    stage = stage,
                    sentAt = now,
                )
            )
            eventPublisher.publishEvent(ChecklistReminderDueEvent(accountId = driverId, stage = stage))
        }
        return driverIds.size
    }
}
