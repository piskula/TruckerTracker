package sk.momosilabs.truckTrack.account.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Entity(name = "account")
class AccountEntity(

    @Id // PK is the Keycloak user UUID — assigned externally, never generated here
    val id: UUID,

    @field:NotNull
    var username: String,

    @field:NotNull
    var firstName: String,

    @field:NotNull
    var lastName: String,

)
