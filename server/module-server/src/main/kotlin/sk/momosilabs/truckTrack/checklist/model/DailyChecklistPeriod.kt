package sk.momosilabs.truckTrack.checklist.model

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

object DailyChecklistPeriod : ChecklistPeriod {

    override val alreadySubmittedMessage = "Už ste to dnes vyplnili."

    private val zone: ZoneId = ZoneId.of(ChecklistPeriod.ZONE_ID)

    override fun startDateOf(moment: OffsetDateTime): LocalDate =
        moment.atZoneSameInstant(zone).toLocalDate()
}
