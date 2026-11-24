package sk.momosilabs.truckTrack.security

import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.RemoteKeySourceException
import com.nimbusds.jwt.JWTParser
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.JwtTypeValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.time.Duration

@Service
class LogoutTokenVerifier(
    private val props: KeycloakProperties,
) {

    private val claimsValidator = LogoutTokenClaimsValidator(props)
    private val replayGuard = LogoutTokenReplayGuard()

    private val logoutJwtDecoder: JwtDecoder by lazy {
        NimbusJwtDecoder.withJwkSetUri(props.jwksUri)
            .restOperations(jwksRestTemplate())
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .validateType(false)
            .build()
            .also { decoder ->
                decoder.setJwtValidator(JwtTypeValidator(LOGOUT_TOKEN_TYPE))
            }
    }

    fun verify(logoutToken: String): VerifiedLogoutToken {
        val jwt = decode(logoutToken)
        return try {
            claimsValidator.verify(jwt)
        } catch (exception: ClassCastException) {
            LogoutTokenRejection.rejectClaimCastFailure(exception)
        }
    }

    fun markProcessed(token: VerifiedLogoutToken) {
        replayGuard.markProcessed(token)
    }

    private fun jwksRestTemplate(): RestOperations {
        val requestFactory = SimpleClientHttpRequestFactory()
        requestFactory.setConnectTimeout(JWKS_CONNECT_TIMEOUT)
        requestFactory.setReadTimeout(JWKS_READ_TIMEOUT)
        return RestTemplate(requestFactory)
    }

    private fun decode(logoutToken: String): Jwt =
        try {
            logoutJwtDecoder.decode(logoutToken)
        } catch (exception: JwtException) {
            if (exception.isKeySourceFailure()) {
                LogoutTokenRejection.rejectKeySourceUnavailable(exception)
            }
            val facts = headerFacts(logoutToken)
            LogoutTokenRejection.rejectDecode(decodeRejectionReason(facts), facts, exception.javaClass.simpleName)
        }

    private fun Throwable.isKeySourceFailure(): Boolean {
        var current: Throwable? = this
        var depth = 0
        while (current != null && depth < MAX_CAUSE_DEPTH) {
            if (current is RemoteKeySourceException || current is IOException) {
                return true
            }
            current = current.cause
            depth++
        }
        return false
    }

    private fun decodeRejectionReason(facts: HeaderFacts): String =
        when {
            facts.algorithm == null -> REASON_TOKEN_UNPARSEABLE
            facts.algorithm.equals(UNSIGNED_ALGORITHM, ignoreCase = true) -> REASON_UNSIGNED
            facts.type == null -> REASON_TYPE_MISSING
            !facts.type.equals(LOGOUT_TOKEN_TYPE, ignoreCase = true) -> REASON_TYPE_INVALID
            !facts.algorithm.equals(ACCEPTED_ALGORITHM, ignoreCase = true) -> REASON_ALGORITHM_UNSUPPORTED
            else -> REASON_SIGNATURE_INVALID
        }

    private fun headerFacts(logoutToken: String): HeaderFacts =
        try {
            val header = JWTParser.parse(logoutToken).header
            val jwsHeader = header as? JWSHeader
            HeaderFacts(header.algorithm?.name, jwsHeader?.type?.toString(), jwsHeader?.keyID)
        } catch (_: Exception) {
            HeaderFacts(null, null, null)
        }

    companion object {
        private const val LOGOUT_TOKEN_TYPE = "logout+jwt"
        private const val UNSIGNED_ALGORITHM = "none"
        private const val ACCEPTED_ALGORITHM = "RS256"
        private const val MAX_CAUSE_DEPTH = 10
        private val JWKS_CONNECT_TIMEOUT: Duration = Duration.ofSeconds(5)
        private val JWKS_READ_TIMEOUT: Duration = Duration.ofSeconds(10)
        private const val REASON_TOKEN_UNPARSEABLE = "token_unparseable"
        private const val REASON_UNSIGNED = "unsigned_token"
        private const val REASON_TYPE_MISSING = "typ_missing"
        private const val REASON_TYPE_INVALID = "typ_invalid"
        private const val REASON_ALGORITHM_UNSUPPORTED = "algorithm_unsupported"
        private const val REASON_SIGNATURE_INVALID = "signature_invalid"
    }
}

data class VerifiedLogoutToken(
    val subject: String,
    val jti: String,
    val expiryEpochSecond: Long,
)
