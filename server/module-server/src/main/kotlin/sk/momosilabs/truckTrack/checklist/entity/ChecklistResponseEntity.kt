package sk.momosilabs.truckTrack.checklist.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.ColumnTransformer
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.vehicle.entity.VehicleEntity
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(name = "checklist_response")
class ChecklistResponseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @field:NotNull
    @ManyToOne(optional = false)
    val truck: VehicleEntity,

    @field:NotNull
    @ManyToOne(optional = false)
    val trailer: VehicleEntity,

    @field:NotNull
    @ManyToOne(optional = false)
    val answeredBy: AccountEntity,

    @field:NotNull
    val answeredAtUtc: LocalDateTime,

    val periodStartDate: LocalDate?,

    @field:NotNull
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    val answers: String,

)
