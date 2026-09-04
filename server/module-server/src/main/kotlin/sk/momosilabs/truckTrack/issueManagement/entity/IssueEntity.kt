package sk.momosilabs.truckTrack.issueManagement.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.vehicle.entity.VehicleEntity
import java.time.LocalDateTime

@Entity(name = "issue")
class IssueEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @field:NotNull
    var title: String,

    @field:NotNull
    var description: String,

    @Enumerated(EnumType.STRING)
    @field:NotNull
    var status: IssueStatus,

    @Enumerated(EnumType.STRING)
    @field:NotNull
    var priority: IssuePriority,

    @ManyToOne(optional = false)
    @field:NotNull
    var vehicle: VehicleEntity,

    @ManyToOne(optional = false)
    @field:NotNull
    val reportedBy: AccountEntity,

    @ManyToOne
    var assignedTo: AccountEntity?,

    @field:NotNull
    val createdAtUtc: LocalDateTime,

    @field:NotNull
    var updatedAtUtc: LocalDateTime,

)
