package sk.momosilabs.truckTrack.checklist.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.checklist.model.ChecklistPeriod
import sk.momosilabs.truckTrack.checklist.service.sendChecklistReminder.SendChecklistReminderUseCase

class DailyChecklistReminderScheduler(
    private val sendChecklistReminder: SendChecklistReminderUseCase,
) {

    @Scheduled(cron = "0 0 8 * * *", zone = ChecklistPeriod.ZONE_ID)
    fun remindDaily() {
        val reminded = sendChecklistReminder.send(ChecklistReminderStage.DAILY)
        LOG.info { "Daily checklist reminder queued for $reminded driver(s)" }
    }

    private companion object {
        val LOG = KotlinLogging.logger {}
    }
}
