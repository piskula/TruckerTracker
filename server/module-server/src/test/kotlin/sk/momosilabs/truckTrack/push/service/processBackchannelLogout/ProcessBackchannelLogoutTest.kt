package sk.momosilabs.truckTrack.push.service.processBackchannelLogout

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import sk.momosilabs.truckTrack.config.GlobalBadRequestException
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.security.LogoutTokenRejection
import sk.momosilabs.truckTrack.security.LogoutTokenVerifier
import sk.momosilabs.truckTrack.security.VerifiedLogoutToken
import java.util.UUID

class ProcessBackchannelLogoutTest {

    private val accountId = UUID.randomUUID()
    private val persistence = mock(PushDevicePersistence::class.java)
    private val logoutTokenVerifier = mock(LogoutTokenVerifier::class.java)

    @Test
    fun `a valid logout token deletes all devices of the account and marks the token processed`() {
        val verifiedToken = verifiedToken(accountId.toString())
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken)

        useCase().process(LOGOUT_TOKEN)

        verify(persistence).deleteAllForAccount(accountId)
        verify(logoutTokenVerifier).markProcessed(verifiedToken)
    }

    @Test
    fun `the devices are deleted before the token is marked processed`() {
        val verifiedToken = verifiedToken(accountId.toString())
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken)

        useCase().process(LOGOUT_TOKEN)

        val order = inOrder(logoutTokenVerifier, persistence)
        order.verify(logoutTokenVerifier).verify(LOGOUT_TOKEN)
        order.verify(persistence).deleteAllForAccount(accountId)
        order.verify(logoutTokenVerifier).markProcessed(verifiedToken)
    }

    @Test
    fun `a logout token whose subject is not an account id is rejected without deleting or marking anything`() {
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken("not-an-account-id"))

        assertThatThrownBy { useCase().process(LOGOUT_TOKEN) }
            .isInstanceOf(GlobalBadRequestException::class.java)
            .hasMessage(LogoutTokenRejection.INVALID_LOGOUT_TOKEN_MESSAGE)

        verify(persistence, never()).deleteAllForAccount(anyOrNullAccount())
        verify(logoutTokenVerifier, never()).markProcessed(anyOrNullToken())
    }

    @Test
    fun `a logout token the verifier rejects propagates without deleting any device`() {
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenThrow(GlobalBadRequestException("Invalid logout token"))

        assertThatThrownBy { useCase().process(LOGOUT_TOKEN) }
            .isInstanceOf(GlobalBadRequestException::class.java)

        verifyNoInteractions(persistence)
        verify(logoutTokenVerifier, never()).markProcessed(anyOrNullToken())
    }

    @Test
    fun `a logout token whose devices cannot be deleted propagates the failure without marking the token processed`() {
        val verifiedToken = verifiedToken(accountId.toString())
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken)
        doThrow(RuntimeException(DELETE_FAILURE_MESSAGE)).`when`(persistence).deleteAllForAccount(anyOrNullAccount())

        assertThatThrownBy { useCase().process(LOGOUT_TOKEN) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage(DELETE_FAILURE_MESSAGE)

        verify(persistence).deleteAllForAccount(accountId)
        verify(logoutTokenVerifier, never()).markProcessed(anyOrNullToken())
    }

    private fun useCase() = ProcessBackchannelLogout(persistence, logoutTokenVerifier)

    private fun verifiedToken(subject: String) = VerifiedLogoutToken(
        subject = subject,
        jti = JTI,
        expiryEpochSecond = EXPIRY_EPOCH_SECOND,
    )

    private fun anyOrNullAccount(): UUID = any(UUID::class.java) ?: ANY_ACCOUNT_ID

    private fun anyOrNullToken(): VerifiedLogoutToken = any(VerifiedLogoutToken::class.java) ?: ANY_PROCESSED_TOKEN

    private companion object {
        const val LOGOUT_TOKEN = "eyJhbGciOiJSUzI1NiJ9.logout.token"
        const val JTI = "logout-token-jti"
        const val EXPIRY_EPOCH_SECOND = 1_767_225_600L
        const val DELETE_FAILURE_MESSAGE = "commit failed"
        const val ANY_SUBJECT = "any-subject"
        val ANY_ACCOUNT_ID: UUID = UUID(0, 0)
        val ANY_PROCESSED_TOKEN: VerifiedLogoutToken = VerifiedLogoutToken(
            subject = ANY_SUBJECT,
            jti = JTI,
            expiryEpochSecond = EXPIRY_EPOCH_SECOND,
        )
    }
}
