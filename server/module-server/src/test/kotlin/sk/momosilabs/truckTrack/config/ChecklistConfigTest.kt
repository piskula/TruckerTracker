package sk.momosilabs.truckTrack.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.support.CronExpression
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.checklist.model.ChecklistPeriod
import sk.momosilabs.truckTrack.checklist.model.DailyChecklistPeriod
import sk.momosilabs.truckTrack.checklist.scheduler.ChecklistReminderScheduler
import sk.momosilabs.truckTrack.checklist.scheduler.DailyChecklistReminderScheduler
import sk.momosilabs.truckTrack.checklist.service.sendChecklistReminder.SendChecklistReminderUseCase
import java.time.ZoneId
import java.time.ZonedDateTime

class ChecklistConfigTest {

    private val sentStages = mutableListOf<ChecklistReminderStage>()
    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(ChecklistConfig::class.java, SchedulerScan::class.java)
        .withBean(SendChecklistReminderUseCase::class.java, {
            object : SendChecklistReminderUseCase {
                override fun send(stage: ChecklistReminderStage): Int {
                    sentStages += stage
                    return 1
                }
            }
        })

    @Test
    fun `only the daily scheduler and period are bound and the scheduler sends the daily stage`() {
        contextRunner.withPropertyValues("app.checklist-reminder.enabled=true").run { context ->
            assertThat(context).hasSingleBean(ChecklistPeriod::class.java)
            assertThat(context.getBean(ChecklistPeriod::class.java)).isSameAs(DailyChecklistPeriod)
            assertThat(context).hasSingleBean(DailyChecklistReminderScheduler::class.java)
            assertThat(context).doesNotHaveBean(ChecklistReminderScheduler::class.java)

            context.getBean(DailyChecklistReminderScheduler::class.java).remindDaily()

            assertThat(sentStages).containsExactly(ChecklistReminderStage.DAILY)
        }
    }

    @Test
    fun `the existing off switch disables reminders without changing the daily checklist period`() {
        contextRunner.withPropertyValues("app.checklist-reminder.enabled=false").run { context ->
            assertThat(context).doesNotHaveBean(DailyChecklistReminderScheduler::class.java)
            assertThat(context.getBean(ChecklistPeriod::class.java)).isSameAs(DailyChecklistPeriod)
        }
    }

    @Test
    fun `daily schedule runs at eight Bratislava time on every day including weekends across daylight saving`() {
        val scheduled = DailyChecklistReminderScheduler::class.java.getMethod("remindDaily").getAnnotation(Scheduled::class.java)
        val cron = CronExpression.parse(scheduled.cron)
        val zone = ZoneId.of(scheduled.zone)
        var previous = ZonedDateTime.of(2026, 10, 23, 8, 0, 0, 0, zone)

        repeat(7) {
            val next = checkNotNull(cron.next(previous))
            assertThat(next.toLocalDate()).isEqualTo(previous.toLocalDate().plusDays(1))
            assertThat(next.hour).isEqualTo(8)
            assertThat(next.minute).isZero()
            assertThat(next.second).isZero()
            assertThat(next.zone).isEqualTo(ZoneId.of("Europe/Bratislava"))
            previous = next
        }
    }

    @Configuration
    @ComponentScan(basePackageClasses = [ChecklistReminderScheduler::class])
    class SchedulerScan
}
