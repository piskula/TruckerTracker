package sk.momosilabs.truckTrack.checklist.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(name = "checklist_reminder")
class ChecklistReminderEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(optional = false)
    @field:NotNull
    val account: AccountEntity,

    @field:NotNull
    val periodStartDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @field:NotNull
    val stage: ChecklistReminderStage,

    @field:NotNull
    val sentAtUtc: LocalDateTime,

)
