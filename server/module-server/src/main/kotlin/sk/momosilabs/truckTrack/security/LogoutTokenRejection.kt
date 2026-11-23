package sk.momosilabs.truckTrack.security

import io.github.oshai.kotlinlogging.KotlinLogging
import sk.momosilabs.truckTrack.config.GlobalBadRequestException
import sk.momosilabs.truckTrack.config.GlobalServerErrorException

internal object LogoutTokenRejection {

    private val LOG = KotlinLogging.logger {}
    private const val REJECT_LOG_PREFIX = "Rejected OIDC back-channel logout token"
    const val INVALID_LOGOUT_TOKEN_MESSAGE = "Invalid logout token"
    private const val REASON_KEY_SOURCE_UNAVAILABLE = "key_source_unavailable"
    private const val REASON_CLAIM_TYPE_INVALID = "claim_type_invalid"
    private const val REASON_CLAIM_CAST_FAILURE = "claim_cast_failure"
    private const val UNSAFE_HEADER_FACT_PLACEHOLDER = "invalid"
    private val SAFE_HEADER_FACT = Regex("[A-Za-z0-9_-]{1,64}")

    fun reject(reason: String): Nothing {
        LOG.warn { "$REJECT_LOG_PREFIX reason=$reason" }
        throw GlobalBadRequestException(INVALID_LOGOUT_TOKEN_MESSAGE)
    }

    fun rejectInvalidClaimType(claim: String): Nothing {
        LOG.warn { "$REJECT_LOG_PREFIX reason=$REASON_CLAIM_TYPE_INVALID claim=$claim" }
        throw GlobalBadRequestException(INVALID_LOGOUT_TOKEN_MESSAGE)
    }

    fun rejectClaimCastFailure(cause: Throwable): Nothing {
        LOG.warn(cause) { "$REJECT_LOG_PREFIX reason=$REASON_CLAIM_CAST_FAILURE" }
        throw GlobalBadRequestException(INVALID_LOGOUT_TOKEN_MESSAGE)
    }

    fun rejectDecode(reason: String, facts: HeaderFacts, validatorName: String): Nothing {
        LOG.warn {
            "$REJECT_LOG_PREFIX reason=$reason alg=${safeHeaderFact(facts.algorithm)} " +
                "typ=${safeHeaderFact(facts.type)} kid=${safeHeaderFact(facts.keyId)} validator=$validatorName"
        }
        throw GlobalBadRequestException(INVALID_LOGOUT_TOKEN_MESSAGE)
    }

    fun rejectKeySourceUnavailable(cause: Throwable): Nothing {
        LOG.warn(cause) { "$REJECT_LOG_PREFIX reason=$REASON_KEY_SOURCE_UNAVAILABLE" }
        throw GlobalServerErrorException(INVALID_LOGOUT_TOKEN_MESSAGE, cause = cause)
    }

    private fun safeHeaderFact(value: String?): String? {
        if (value == null) {
            return null
        }
        return if (SAFE_HEADER_FACT.matches(value)) value else UNSAFE_HEADER_FACT_PLACEHOLDER
    }
}

internal data class HeaderFacts(
    val algorithm: String?,
    val type: String?,
    val keyId: String?,
)
