package sk.momosilabs.truckTrack.push.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import java.time.LocalDateTime

@Entity(name = "push_registration")
class PushRegistrationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(optional = false)
    @field:NotNull
    val account: AccountEntity,

    @field:NotNull
    var installationId: String,

    @Enumerated(EnumType.STRING)
    @field:NotNull
    var platform: PushPlatform,

    @field:NotNull
    var deviceLabel: String,

    @field:NotNull
    var registeredAtUtc: LocalDateTime,

)
