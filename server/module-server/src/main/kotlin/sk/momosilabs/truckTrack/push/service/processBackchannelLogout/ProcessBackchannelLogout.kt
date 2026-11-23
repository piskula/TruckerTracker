package sk.momosilabs.truckTrack.push.service.processBackchannelLogout

import org.springframework.stereotype.Service
import sk.momosilabs.truckTrack.push.service.PushRegistrationPersistence
import sk.momosilabs.truckTrack.security.LogoutTokenRejection
import sk.momosilabs.truckTrack.security.LogoutTokenVerifier
import java.util.UUID

@Service
class ProcessBackchannelLogout(
    private val persistence: PushRegistrationPersistence,
    private val logoutTokenVerifier: LogoutTokenVerifier,
) : ProcessBackchannelLogoutUseCase {

    override fun process(logoutToken: String) {
        val verifiedToken = logoutTokenVerifier.verify(logoutToken)
        val accountId = verifiedToken.subject.toAccountIdOrThrow()
        persistence.deleteRegistrationsOfAccountId(accountId)
        logoutTokenVerifier.markProcessed(verifiedToken)
    }

    private fun String.toAccountIdOrThrow(): UUID =
        try {
            UUID.fromString(this)
        } catch (_: IllegalArgumentException) {
            LogoutTokenRejection.reject(REASON_SUBJECT_NOT_UUID)
        }

    companion object {
        private const val REASON_SUBJECT_NOT_UUID = "sub_not_uuid"
    }
}
