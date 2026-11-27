package sk.momosilabs.truckTrack.checklist.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import sk.momosilabs.truckTrack.checklist.entity.ChecklistResponseEntity
import java.time.LocalDate
import java.util.UUID

interface ChecklistResponseRepository : JpaRepository<ChecklistResponseEntity, Long> {

    fun existsByAnsweredByIdAndPeriodStartDate(answeredById: UUID, periodStartDate: LocalDate): Boolean
}
