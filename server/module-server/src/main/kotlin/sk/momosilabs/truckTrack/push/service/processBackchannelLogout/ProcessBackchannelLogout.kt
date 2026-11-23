package sk.momosilabs.truckTrack.push.service.processBackchannelLogout

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import sk.momosilabs.truckTrack.config.GlobalBadRequestException
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.security.LogoutTokenVerifier
import java.util.UUID

@Service
class ProcessBackchannelLogout(
    private val persistence: PushDevicePersistence,
    private val logoutTokenVerifier: LogoutTokenVerifier,
) : ProcessBackchannelLogoutUseCase {

    override fun process(logoutToken: String) {
        val verifiedToken = logoutTokenVerifier.verify(logoutToken)
        persistence.deleteAllForAccount(verifiedToken.subject.toAccountIdOrThrow())
        logoutTokenVerifier.markProcessed(verifiedToken)
    }

    private fun String.toAccountIdOrThrow(): UUID =
        try {
            UUID.fromString(this)
        } catch (_: IllegalArgumentException) {
            LOG.warn { "Rejected OIDC back-channel logout token reason=$REASON_SUBJECT_NOT_UUID" }
            throw GlobalBadRequestException(INVALID_LOGOUT_TOKEN_MESSAGE)
        }

    companion object {
        private val LOG = KotlinLogging.logger {}
        private const val INVALID_LOGOUT_TOKEN_MESSAGE = "Invalid logout token"
        private const val REASON_SUBJECT_NOT_UUID = "sub_not_uuid"
    }
}
