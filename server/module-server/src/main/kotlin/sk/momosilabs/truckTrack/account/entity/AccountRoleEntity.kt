package sk.momosilabs.truckTrack.account.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull

@Entity(name = "account_role")
class AccountRoleEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(optional = false)
    @field:NotNull
    val account: AccountEntity,

    @Enumerated(EnumType.STRING)
    @field:NotNull
    val role: AccountRole,

)
