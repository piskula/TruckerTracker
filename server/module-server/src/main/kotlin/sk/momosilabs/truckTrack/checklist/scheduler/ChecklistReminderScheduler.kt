package sk.momosilabs.truckTrack.checklist.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.checklist.model.ChecklistPeriod
import sk.momosilabs.truckTrack.checklist.service.sendChecklistReminder.SendChecklistReminderUseCase

@Component
@ConditionalOnProperty(prefix = "app.checklist-reminder", name = ["enabled"], havingValue = "true")
class ChecklistReminderScheduler(
    private val sendChecklistReminder: SendChecklistReminderUseCase,
) {

    @Scheduled(cron = WEEK_START_CRON, zone = ChecklistPeriod.ZONE_ID)
    fun remindAtWeekStart() = remind(ChecklistReminderStage.WEEK_START)

    @Scheduled(cron = TWO_DAYS_LEFT_CRON, zone = ChecklistPeriod.ZONE_ID)
    fun remindTwoDaysBeforeDeadline() = remind(ChecklistReminderStage.TWO_DAYS_LEFT)

    private fun remind(stage: ChecklistReminderStage) {
        val reminded = sendChecklistReminder.send(stage)
        LOG.info { "Checklist reminder $stage queued for $reminded driver(s)" }
    }

    private companion object {
        val LOG = KotlinLogging.logger {}
        const val WEEK_START_CRON = "0 0 8 * * MON"
        const val TWO_DAYS_LEFT_CRON = "0 0 8 * * WED"
    }
}
