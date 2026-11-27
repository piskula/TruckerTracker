package sk.momosilabs.truckTrack.checklist.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object WeeklyChecklistPeriod : ChecklistPeriod {

    override val alreadySubmittedMessage = "Už ste to tento týždeň vyplnili."

    private val zone: ZoneId = ZoneId.of(ChecklistPeriod.ZONE_ID)

    override fun startDateOf(moment: OffsetDateTime): LocalDate =
        moment.atZoneSameInstant(zone).toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
