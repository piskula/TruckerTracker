package sk.momosilabs.truckTrack.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import sk.momosilabs.truckTrack.config.GlobalBadRequestException
import java.time.Instant

class LogoutTokenClaimsValidatorTest {

    private val props = KeycloakProperties(
        issuer = ISSUER,
        jwksUri = JWKS_URI,
        audience = AUDIENCE,
    )
    private val validator = LogoutTokenClaimsValidator(props)

    @Test
    fun `a logout token with every claim valid is accepted and reports its subject jti and expiry`() {
        val expiresAt = Instant.now().plusSeconds(TOKEN_LIFETIME_SECONDS)

        val verified = validator.verify(logoutJwt(expiresAt = expiresAt))

        assertThat(verified.subject).isEqualTo(SUBJECT)
        assertThat(verified.jti).isEqualTo(JTI)
        assertThat(verified.expiryEpochSecond).isEqualTo(expiresAt.epochSecond + CLOCK_SKEW_SECONDS)
    }

    @Test
    fun `a logout token issued by an untrusted issuer is rejected`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_ISSUER to FOREIGN_ISSUER))

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token addressed to a different audience is rejected`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_AUDIENCE to FOREIGN_AUDIENCE))

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token addressed to several audiences is rejected`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_AUDIENCE to listOf(AUDIENCE, FOREIGN_AUDIENCE)))

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token whose audience is a single-element collection is accepted`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_AUDIENCE to listOf(AUDIENCE)))

        assertThatCode { validator.verify(jwt) }.doesNotThrowAnyException()
    }

    @Test
    fun `a logout token whose audience is a single-element collection of a foreign audience is rejected`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_AUDIENCE to listOf(FOREIGN_AUDIENCE)))

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token without an issued-at claim is rejected`() {
        val jwt = logoutJwt(issuedAt = null)

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token without an expires-at claim is rejected`() {
        val jwt = logoutJwt(expiresAt = null)

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token that expired beyond the clock skew tolerance is rejected`() {
        val now = Instant.now()
        val jwt = logoutJwt(
            issuedAt = now.minusSeconds(TOKEN_LIFETIME_SECONDS + EXPIRED_BY_SECONDS),
            expiresAt = now.minusSeconds(EXPIRED_BY_SECONDS),
        )

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token that expired inside the clock skew tolerance is accepted`() {
        val now = Instant.now()
        val jwt = logoutJwt(
            issuedAt = now.minusSeconds(CLOCK_SKEW_SECONDS),
            expiresAt = now.minusSeconds(CLOCK_SKEW_SECONDS / 2),
        )

        assertThatCode { validator.verify(jwt) }.doesNotThrowAnyException()
    }

    @Test
    fun `a logout token issued in the future beyond the clock skew tolerance is rejected`() {
        val jwt = logoutJwt(issuedAt = Instant.now().plusSeconds(CLOCK_SKEW_SECONDS * 2))

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token issued in the future within the clock skew tolerance is accepted`() {
        val jwt = logoutJwt(issuedAt = Instant.now().plusSeconds(CLOCK_SKEW_SECONDS / 2))

        assertThatCode { validator.verify(jwt) }.doesNotThrowAnyException()
    }

    @Test
    fun `a logout token whose events claim lacks the backchannel logout event is rejected`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_EVENTS to mapOf(UNRELATED_EVENT_KEY to emptyMap<String, Any>())))

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token whose events claim carries the backchannel logout event is accepted`() {
        val events = mapOf(
            BACKCHANNEL_LOGOUT_EVENT_KEY to mapOf("issuer" to ISSUER),
            UNRELATED_EVENT_KEY to emptyMap<String, Any>(),
        )
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_EVENTS to events))

        assertThatCode { validator.verify(jwt) }.doesNotThrowAnyException()
    }

    @Test
    fun `a logout token carrying a nonce claim is rejected`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_NONCE to NONCE))

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token without a subject is rejected`() {
        val jwt = logoutJwt(claims = validClaims() - CLAIM_SUBJECT)

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token with a blank subject is rejected`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_SUBJECT to BLANK))

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token without a jti is rejected`() {
        val jwt = logoutJwt(claims = validClaims() - CLAIM_JTI)

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token with a blank jti is rejected`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_JTI to BLANK))

        assertThatRejected(jwt)
    }

    @Test
    fun `a logout token whose subject claim is not a string is rejected`() {
        val jwt = logoutJwt(claims = validClaims() + (CLAIM_SUBJECT to NON_STRING_SUBJECT))

        assertThatRejected(jwt)
    }

    private fun assertThatRejected(jwt: Jwt) {
        assertThatThrownBy { validator.verify(jwt) }
            .isInstanceOf(GlobalBadRequestException::class.java)
            .hasMessage(LogoutTokenRejection.INVALID_LOGOUT_TOKEN_MESSAGE)
    }

    private fun logoutJwt(
        claims: Map<String, Any> = validClaims(),
        issuedAt: Instant? = Instant.now(),
        expiresAt: Instant? = Instant.now().plusSeconds(TOKEN_LIFETIME_SECONDS),
    ): Jwt {
        val allClaims = claims.toMutableMap()
        issuedAt?.let { allClaims[CLAIM_ISSUED_AT] = it.epochSecond }
        expiresAt?.let { allClaims[CLAIM_EXPIRES_AT] = it.epochSecond }
        return Jwt(TOKEN_VALUE, issuedAt, expiresAt, HEADERS, allClaims)
    }

    private fun validClaims(): Map<String, Any> = mapOf(
        CLAIM_ISSUER to ISSUER,
        CLAIM_AUDIENCE to AUDIENCE,
        CLAIM_SUBJECT to SUBJECT,
        CLAIM_JTI to JTI,
        CLAIM_EVENTS to BACKCHANNEL_LOGOUT_EVENTS,
    )

    private companion object {
        const val ISSUER = "https://sso.momosi.org/realms/trucktrack"
        const val JWKS_URI = "https://sso.momosi.org/realms/trucktrack/protocol/openid-connect/certs"
        const val AUDIENCE = "trucktrack-app"
        const val FOREIGN_ISSUER = "https://sso.momosi.org/realms/other"
        const val FOREIGN_AUDIENCE = "trucktrack-admin"
        const val SUBJECT = "3f9e1c2a-7b64-4d0f-9a51-6e2c8b4d7f10"
        const val JTI = "logout-token-jti"
        const val NONCE = "a-nonce-from-an-auth-request"
        const val BLANK = "   "
        const val TOKEN_VALUE = "eyJhbGciOiJSUzI1NiIsInR5cCI6ImxvZ291dCtqd3QifQ.logout-token.signature"
        const val CLAIM_ISSUER = "iss"
        const val CLAIM_AUDIENCE = "aud"
        const val CLAIM_SUBJECT = "sub"
        const val CLAIM_JTI = "jti"
        const val CLAIM_EVENTS = "events"
        const val CLAIM_NONCE = "nonce"
        const val CLAIM_ISSUED_AT = "iat"
        const val CLAIM_EXPIRES_AT = "exp"
        const val BACKCHANNEL_LOGOUT_EVENT_KEY = "http://schemas.openid.net/event/backchannel-logout"
        const val UNRELATED_EVENT_KEY = "http://schemas.openid.net/event/other"
        const val CLOCK_SKEW_SECONDS = 60L
        const val TOKEN_LIFETIME_SECONDS = 300L
        const val EXPIRED_BY_SECONDS = 300L
        const val NON_STRING_SUBJECT: Long = 1_234_567L
        val HEADERS: Map<String, String> = mapOf("alg" to "RS256", "typ" to "logout+jwt")
        val BACKCHANNEL_LOGOUT_EVENTS: Map<String, Any> = mapOf(BACKCHANNEL_LOGOUT_EVENT_KEY to emptyMap<String, Any>())
    }
}
