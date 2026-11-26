package sk.momosilabs.truckTrack.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import sk.momosilabs.truckTrack.account.entity.AccountRole
import sk.momosilabs.truckTrack.account.persistence.AccountPersistenceHelper
import sk.momosilabs.truckTrack.config.GlobalBadRequestException

class CurrentUserServiceTest {

    private val accountPersistenceHelper = mock(AccountPersistenceHelper::class.java)
    private val service = CurrentUserService(accountPersistenceHelper)

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `the account is synced with the role from the verified token, driver taking precedence`() {
        mapOf(
            listOf("ROLE_DRIVER") to AccountRole.DRIVER,
            listOf("ROLE_MECHANIC") to AccountRole.MECHANIC,
            listOf("ROLE_MECHANIC", "ROLE_DRIVER") to AccountRole.DRIVER,
            listOf("offline_access") to null,
        ).forEach { (authorities, role) ->
            authenticate(sessionId = null, authorities = authorities)

            val account = service.currentUser()

            verify(accountPersistenceHelper).sync(account, role)
            reset(accountPersistenceHelper)
        }
    }

    @Test
    fun `push session binding uses the authenticated access token session id`() {
        authenticate("session-a")

        assertThat(service.currentSessionId()).isEqualTo("session-a")
    }

    @Test
    fun `a missing blank or non-string access token session id is rejected`() {
        listOf(null, "", "   ", 42).forEach { sessionId ->
            authenticate(sessionId)

            assertThatThrownBy { service.currentSessionId() }
                .isInstanceOf(GlobalBadRequestException::class.java)
                .hasMessage("Access token must contain a non-blank sid claim")
        }
    }

    private fun authenticate(sessionId: Any?, authorities: List<String> = emptyList()) {
        val builder = Jwt.withTokenValue("access-token")
            .header("alg", "RS256")
            .subject("3f9e1c2a-7b64-4d0f-9a51-6e2c8b4d7f10")
            .claim("preferred_username", "driver")
            .claim("given_name", "Dan")
            .claim("family_name", "Driver")
        sessionId?.let { builder.claim("sid", it) }
        SecurityContextHolder.getContext().authentication =
            JwtAuthenticationToken(builder.build(), authorities.map { SimpleGrantedAuthority(it) })
    }
}
