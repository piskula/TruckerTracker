package sk.momosilabs.truckTrack.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import sk.momosilabs.truckTrack.account.entity.AccountRole
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.account.persistence.AccountPersistenceHelper
import sk.momosilabs.truckTrack.config.GlobalBadRequestException
import java.util.UUID

@Service
class CurrentUserService(
    private val accountPersistenceHelper: AccountPersistenceHelper,
) {

    fun currentUserId(): UUID = jwt().subject.let(UUID::fromString)

    fun currentSessionId(): String =
        (jwt().claims["sid"] as? String)?.takeIf { it.isNotBlank() }
            ?: throw GlobalBadRequestException("Access token must contain a non-blank sid claim")

    fun isMechanic(): Boolean = authorityOf(AccountRole.MECHANIC) in authorities()

    fun isDriver(): Boolean = authorityOf(AccountRole.DRIVER) in authorities()

    fun currentUser(): AccountModel {
        val token = jwt()
        val account = AccountModel(
            id = UUID.fromString(token.subject),
            username = token.getClaimAsString("preferred_username"),
            firstName = token.getClaimAsString("given_name"),
            lastName = token.getClaimAsString("family_name"),
        )
        accountPersistenceHelper.sync(account, currentRoles())
        return account
    }

    private fun currentRoles(): Set<AccountRole> {
        val authorities = authorities()
        return AccountRole.entries.filterTo(HashSet()) { authorityOf(it) in authorities }
    }

    private fun authorityOf(role: AccountRole) = "$ROLE_AUTHORITY_PREFIX${role.name}"

    private fun authorities(): Set<String> =
        authentication().authorities.mapNotNullTo(HashSet()) { it.authority }

    private fun jwt() = authentication().token

    private fun authentication() =
        SecurityContextHolder.getContext().authentication as JwtAuthenticationToken

    private companion object {
        const val ROLE_AUTHORITY_PREFIX = "ROLE_"
    }
}
