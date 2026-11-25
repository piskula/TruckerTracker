package sk.momosilabs.truckTrack.push.entity

import jakarta.persistence.Column
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

@Entity(name = "push_device")
class PushDeviceEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(optional = false)
    @field:NotNull
    val account: AccountEntity,

    @field:NotNull
    var firebaseInstallationId: String,

    @field:NotNull
    val deviceName: String,

    @Enumerated(EnumType.STRING)
    @field:NotNull
    val platform: PushPlatform,

    @field:NotNull
    val registeredAtUtc: LocalDateTime,

    @field:NotNull
    var updatedAtUtc: LocalDateTime,

    @Column(columnDefinition = "text")
    val sessionId: String? = null,

)
