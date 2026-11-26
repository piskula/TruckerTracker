package sk.momosilabs.truckTrack.checklist.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import sk.momosilabs.truckTrack.account.entity.AccountRole
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderEntity
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import java.time.LocalDate
import java.util.UUID

interface ChecklistReminderRepository : JpaRepository<ChecklistReminderEntity, Long> {

    @Query(
        """
        select a.id from account a
        where a.role = :role
          and not exists (
            select 1 from checklist_response r
            where r.answeredBy = a and r.periodStartDate = :periodStartDate
          )
          and not exists (
            select 1 from checklist_reminder m
            where m.account = a and m.periodStartDate = :periodStartDate and m.stage = :stage
          )
        order by a.id
        """
    )
    fun findAccountIdsDueForReminder(role: AccountRole, periodStartDate: LocalDate, stage: ChecklistReminderStage): List<UUID>
}
