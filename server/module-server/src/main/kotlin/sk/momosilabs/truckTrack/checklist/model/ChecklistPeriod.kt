package sk.momosilabs.truckTrack.checklist.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object ChecklistPeriod {

    const val ZONE_ID = "Europe/Bratislava"

    private val ZONE: ZoneId = ZoneId.of(ZONE_ID)

    fun startDateOf(moment: OffsetDateTime): LocalDate =
        moment.atZoneSameInstant(ZONE).toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
