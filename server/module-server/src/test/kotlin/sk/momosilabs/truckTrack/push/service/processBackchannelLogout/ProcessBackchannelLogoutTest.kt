package sk.momosilabs.truckTrack.push.service.processBackchannelLogout

import org.assertj.core.api.Assertions.assertThat
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
import sk.momosilabs.truckTrack.push.entity.PushPlatform
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.service.FakePushDevicePersistence
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.security.LogoutTokenRejection
import sk.momosilabs.truckTrack.security.LogoutTokenVerifier
import sk.momosilabs.truckTrack.security.VerifiedLogoutToken
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class ProcessBackchannelLogoutTest {

    private val accountId = UUID.randomUUID()
    private val persistence = mock(PushDevicePersistence::class.java)
    private val logoutTokenVerifier = mock(LogoutTokenVerifier::class.java)

    @Test
    fun `a valid logout token deletes only the account session and marks the token processed`() {
        val verifiedToken = verifiedToken(accountId.toString())
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken)

        useCase().process(LOGOUT_TOKEN)

        verify(persistence).deleteByAccountIdAndSessionId(accountId, SESSION_ID)
        verify(logoutTokenVerifier).markProcessed(verifiedToken)
    }

    @Test
    fun `the devices are deleted before the token is marked processed`() {
        val verifiedToken = verifiedToken(accountId.toString())
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken)

        useCase().process(LOGOUT_TOKEN)

        val order = inOrder(logoutTokenVerifier, persistence)
        order.verify(logoutTokenVerifier).verify(LOGOUT_TOKEN)
        order.verify(persistence).deleteByAccountIdAndSessionId(accountId, SESSION_ID)
        order.verify(logoutTokenVerifier).markProcessed(verifiedToken)
    }

    @Test
    fun `a logout token whose subject is not an account id is rejected without deleting or marking anything`() {
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken("not-an-account-id"))

        assertThatThrownBy { useCase().process(LOGOUT_TOKEN) }
            .isInstanceOf(GlobalBadRequestException::class.java)
            .hasMessage(LogoutTokenRejection.INVALID_LOGOUT_TOKEN_MESSAGE)

        verifyNoInteractions(persistence)
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
        doThrow(RuntimeException(DELETE_FAILURE_MESSAGE)).`when`(persistence).deleteByAccountIdAndSessionId(accountId, SESSION_ID)

        assertThatThrownBy { useCase().process(LOGOUT_TOKEN) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage(DELETE_FAILURE_MESSAGE)

        verify(persistence).deleteByAccountIdAndSessionId(accountId, SESSION_ID)
        verify(logoutTokenVerifier, never()).markProcessed(anyOrNullToken())
    }

    @Test
    fun `logging out device B leaves device A registered for the same user`() {
        val deviceA = device("installation-a", SESSION_ID)
        val devices = FakePushDevicePersistence(deviceA)
        val verifiedToken = verifiedToken(accountId.toString(), OTHER_SESSION_ID)
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken)

        ProcessBackchannelLogout(devices, logoutTokenVerifier).process(LOGOUT_TOKEN)

        assertThat(devices.devices).containsExactly(deviceA)
        verify(logoutTokenVerifier).markProcessed(verifiedToken)
    }

    @Test
    fun `logging out device A removes its registration but preserves other sessions accounts and unbound legacy rows`() {
        val deviceA = device("installation-a", SESSION_ID)
        val deviceB = device("installation-b", OTHER_SESSION_ID)
        val otherAccount = device("installation-other-account", SESSION_ID).copy(accountId = UUID.randomUUID())
        val legacyDevice = device("installation-legacy", null)
        val devices = FakePushDevicePersistence(deviceA, deviceB, otherAccount, legacyDevice)
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken(accountId.toString()))

        ProcessBackchannelLogout(devices, logoutTokenVerifier).process(LOGOUT_TOKEN)

        assertThat(devices.devices).containsExactly(deviceB, otherAccount, legacyDevice)
    }

    @Test
    fun `a delayed logout for an old session leaves the same device registered under its new session`() {
        val newlyRegistered = device("installation-a", OTHER_SESSION_ID)
        val devices = FakePushDevicePersistence(newlyRegistered)
        `when`(logoutTokenVerifier.verify(LOGOUT_TOKEN)).thenReturn(verifiedToken(accountId.toString()))

        ProcessBackchannelLogout(devices, logoutTokenVerifier).process(LOGOUT_TOKEN)

        assertThat(devices.devices).containsExactly(newlyRegistered)
    }

    private fun useCase() = ProcessBackchannelLogout(persistence, logoutTokenVerifier)

    private fun verifiedToken(subject: String, sessionId: String = SESSION_ID) = VerifiedLogoutToken(
        subject = subject,
        jti = JTI,
        expiryEpochSecond = EXPIRY_EPOCH_SECOND,
        sessionId = sessionId,
    )

    private fun device(firebaseInstallationId: String, sessionId: String?) = PushDeviceModel(
        id = 0L,
        accountId = accountId,
        firebaseInstallationId = firebaseInstallationId,
        deviceName = "Phone",
        platform = PushPlatform.ANDROID,
        registeredAt = OffsetDateTime.of(2026, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC),
        updatedAt = OffsetDateTime.of(2026, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC),
        sessionId = sessionId,
    )

    private fun anyOrNullToken(): VerifiedLogoutToken = any(VerifiedLogoutToken::class.java) ?: ANY_PROCESSED_TOKEN

    private companion object {
        const val LOGOUT_TOKEN = "eyJhbGciOiJSUzI1NiJ9.logout.token"
        const val JTI = "logout-token-jti"
        const val EXPIRY_EPOCH_SECOND = 1_767_225_600L
        const val DELETE_FAILURE_MESSAGE = "commit failed"
        const val ANY_SUBJECT = "any-subject"
        const val SESSION_ID = "session-a"
        const val OTHER_SESSION_ID = "session-b"
        val ANY_PROCESSED_TOKEN: VerifiedLogoutToken = VerifiedLogoutToken(
            subject = ANY_SUBJECT,
            jti = JTI,
            expiryEpochSecond = EXPIRY_EPOCH_SECOND,
            sessionId = SESSION_ID,
        )
    }
}
