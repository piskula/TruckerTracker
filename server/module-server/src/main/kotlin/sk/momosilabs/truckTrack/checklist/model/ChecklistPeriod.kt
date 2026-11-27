package sk.momosilabs.truckTrack.checklist.model

import java.time.LocalDate
import java.time.OffsetDateTime

interface ChecklistPeriod {

    val alreadySubmittedMessage: String

    fun startDateOf(moment: OffsetDateTime): LocalDate

    companion object {
        const val ZONE_ID = "Europe/Bratislava"
    }
}
