package sk.momosilabs.truckTrack.security

import org.springframework.security.oauth2.jwt.Jwt
import java.time.Duration
import java.time.Instant

internal class LogoutTokenClaimsValidator(
    private val props: KeycloakProperties,
) {

    fun verify(jwt: Jwt): VerifiedLogoutToken {
        validateIssuer(jwt)
        validateAudience(jwt)
        validateTimestamps(jwt)
        validateEvents(jwt)
        validateNonceAbsent(jwt)
        val subject = subject(jwt)
        val jti = jti(jwt)
        val expiryEpochSecond = (jwt.expiresAt ?: Instant.now().plus(CLOCK_SKEW)).epochSecond + CLOCK_SKEW.seconds
        return VerifiedLogoutToken(subject, jti, expiryEpochSecond)
    }

    private fun validateIssuer(jwt: Jwt) {
        if (stringClaim(jwt, CLAIM_ISSUER) != props.issuer) {
            LogoutTokenRejection.reject(REASON_ISSUER_MISMATCH)
        }
    }

    private fun validateAudience(jwt: Jwt) {
        val trusted = when (val value = jwt.claims[CLAIM_AUDIENCE]) {
            is String -> value == props.audience
            is Collection<*> -> value.size == 1 && value.first() == props.audience
            else -> false
        }
        if (!trusted) {
            LogoutTokenRejection.reject(REASON_AUDIENCE_MISMATCH)
        }
    }

    private fun validateTimestamps(jwt: Jwt) {
        val now = Instant.now()
        val issuedAt = jwt.issuedAt ?: LogoutTokenRejection.reject(REASON_ISSUED_AT_MISSING)
        val expiresAt = jwt.expiresAt ?: LogoutTokenRejection.reject(REASON_EXPIRES_AT_MISSING)
        if (now.isAfter(expiresAt.plus(CLOCK_SKEW))) {
            LogoutTokenRejection.reject(REASON_EXPIRED)
        }
        if (issuedAt.isAfter(now.plus(CLOCK_SKEW))) {
            LogoutTokenRejection.reject(REASON_NOT_YET_VALID)
        }
    }

    private fun validateEvents(jwt: Jwt) {
        val events = jwt.claims[CLAIM_EVENTS] as? Map<*, *>
        val logoutEvent = events?.get(BACKCHANNEL_LOGOUT_EVENT_KEY)
        if (logoutEvent !is Map<*, *>) {
            LogoutTokenRejection.reject(REASON_EVENTS_MISSING)
        }
    }

    private fun validateNonceAbsent(jwt: Jwt) {
        if (jwt.claims.containsKey(CLAIM_NONCE)) {
            LogoutTokenRejection.reject(REASON_NONCE_PRESENT)
        }
    }

    private fun subject(jwt: Jwt): String =
        stringClaim(jwt, CLAIM_SUBJECT)?.takeIf { it.isNotBlank() }
            ?: LogoutTokenRejection.reject(REASON_SUBJECT_MISSING)

    private fun jti(jwt: Jwt): String =
        stringClaim(jwt, CLAIM_JTI)?.takeIf { it.isNotBlank() }
            ?: LogoutTokenRejection.reject(REASON_JTI_MISSING)

    private fun stringClaim(jwt: Jwt, name: String): String? =
        when (val value = jwt.claims[name]) {
            null -> null
            is String -> value
            else -> LogoutTokenRejection.rejectInvalidClaimType(name)
        }

    companion object {
        private const val BACKCHANNEL_LOGOUT_EVENT_KEY = "http://schemas.openid.net/event/backchannel-logout"
        private const val CLAIM_ISSUER = "iss"
        private const val CLAIM_AUDIENCE = "aud"
        private const val CLAIM_EVENTS = "events"
        private const val CLAIM_NONCE = "nonce"
        private const val CLAIM_SUBJECT = "sub"
        private const val CLAIM_JTI = "jti"
        private val CLOCK_SKEW: Duration = Duration.ofSeconds(60)
        private const val REASON_ISSUER_MISMATCH = "issuer_mismatch"
        private const val REASON_AUDIENCE_MISMATCH = "audience_mismatch"
        private const val REASON_ISSUED_AT_MISSING = "issued_at_missing"
        private const val REASON_EXPIRES_AT_MISSING = "expires_at_missing"
        private const val REASON_EXPIRED = "expired"
        private const val REASON_NOT_YET_VALID = "issued_at_in_future"
        private const val REASON_EVENTS_MISSING = "events_claim_missing"
        private const val REASON_NONCE_PRESENT = "nonce_present"
        private const val REASON_SUBJECT_MISSING = "subject_missing"
        private const val REASON_JTI_MISSING = "jti_missing"
    }
}
