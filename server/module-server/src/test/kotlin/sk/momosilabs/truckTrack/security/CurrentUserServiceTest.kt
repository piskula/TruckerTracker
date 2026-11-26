package sk.momosilabs.truckTrack.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import sk.momosilabs.truckTrack.account.persistence.AccountPersistenceHelper
import sk.momosilabs.truckTrack.config.GlobalBadRequestException

class CurrentUserServiceTest {

    private val service = CurrentUserService(mock(AccountPersistenceHelper::class.java))

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
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

    private fun authenticate(sessionId: Any?) {
        val builder = Jwt.withTokenValue("access-token")
            .header("alg", "RS256")
            .subject("3f9e1c2a-7b64-4d0f-9a51-6e2c8b4d7f10")
        sessionId?.let { builder.claim("sid", it) }
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(builder.build())
    }
}
