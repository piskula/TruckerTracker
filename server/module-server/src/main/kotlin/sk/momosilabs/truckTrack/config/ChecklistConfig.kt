package sk.momosilabs.truckTrack.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sk.momosilabs.truckTrack.checklist.model.ChecklistPeriod
import sk.momosilabs.truckTrack.checklist.model.DailyChecklistPeriod
import sk.momosilabs.truckTrack.checklist.scheduler.DailyChecklistReminderScheduler
import sk.momosilabs.truckTrack.checklist.service.sendChecklistReminder.SendChecklistReminderUseCase

@Configuration
class ChecklistConfig {

    @Bean
    fun checklistPeriod(): ChecklistPeriod = DailyChecklistPeriod

    @Bean
    @ConditionalOnProperty(prefix = "app.checklist-reminder", name = ["enabled"], havingValue = "true")
    fun checklistReminderScheduler(sendChecklistReminder: SendChecklistReminderUseCase): DailyChecklistReminderScheduler =
        DailyChecklistReminderScheduler(sendChecklistReminder)
}
