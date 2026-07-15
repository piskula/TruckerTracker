package sk.momosilabs.truckTrack.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.account.persistence.AccountPersistenceHelper
import java.util.UUID

@Service
class CurrentUserService(
    private val accountPersistenceHelper: AccountPersistenceHelper,
) {

    fun currentUserId(): UUID = jwt().subject.let(UUID::fromString)

    fun currentUser(): AccountModel {
        val token = jwt()
        val account = AccountModel(
            id = UUID.fromString(token.subject),
            username = token.getClaimAsString("preferred_username")!!,
            firstName = token.getClaimAsString("given_name")!!,
            lastName = token.getClaimAsString("family_name")!!,
        )
        accountPersistenceHelper.sync(account)
        return account
    }

    private fun jwt() =
        (SecurityContextHolder.getContext().authentication as JwtAuthenticationToken).token
}
