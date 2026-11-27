package sk.momosilabs.truckTrack.checklist.service

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.push.model.PushMessage
import sk.momosilabs.truckTrack.push.service.PushNotificationService

@Component
class ChecklistReminderDueListener(
    private val pushNotificationService: PushNotificationService,
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onChecklistReminderDue(event: ChecklistReminderDueEvent) {
        pushNotificationService.sendToAccount(accountId = event.accountId, message = event.stage.toPushMessage())
    }

    private fun ChecklistReminderStage.toPushMessage(): PushMessage =
        when (this) {
            ChecklistReminderStage.WEEK_START -> PushMessage.checklistReminderWeekStart()
            ChecklistReminderStage.TWO_DAYS_LEFT -> PushMessage.checklistReminderTwoDaysLeft()
        }
}
