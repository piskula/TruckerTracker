package sk.momosilabs.truckTrack.push.service.getCurrentUserPushDevice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import sk.momosilabs.truckTrack.push.entity.PushPlatform
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.service.FakePushDevicePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class GetCurrentUserPushDeviceTest {

    private val currentUserId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID()

    @Test
    fun `an account without a registered device has no push device`() {
        val persistence = FakePushDevicePersistence(device(accountId = otherUserId))

        val found = useCase(persistence).get()

        assertThat(found).isNull()
    }

    @Test
    fun `the account returns its registered device`() {
        val device = device(accountId = currentUserId)
        val persistence = FakePushDevicePersistence(device(accountId = otherUserId), device)

        val found = useCase(persistence).get()

        assertThat(found).isEqualTo(device)
    }

    @Test
    fun `the most recently registered device is returned when the account has several`() {
        val older = device(accountId = currentUserId, id = 1L, registeredAt = REGISTERED_AT)
        val newer = device(
            accountId = currentUserId,
            id = 2L,
            firebaseInstallationId = "bFirebaseInstallId2",
            registeredAt = REGISTERED_AT.plusDays(1),
        )
        val persistence = FakePushDevicePersistence(device(accountId = otherUserId), newer, older)

        val found = useCase(persistence).get()

        assertThat(found).isEqualTo(newer)
    }

    private fun useCase(persistence: FakePushDevicePersistence): GetCurrentUserPushDevice {
        val currentUserService = mock(CurrentUserService::class.java)
        `when`(currentUserService.currentUserId()).thenReturn(currentUserId)
        return GetCurrentUserPushDevice(persistence, currentUserService)
    }

    private fun device(
        accountId: UUID,
        id: Long = 1L,
        firebaseInstallationId: String = "aFirebaseInstallId1",
        registeredAt: OffsetDateTime = REGISTERED_AT,
    ) = PushDeviceModel(
        id = id,
        accountId = accountId,
        firebaseInstallationId = firebaseInstallationId,
        deviceName = "Pixel 8",
        platform = PushPlatform.ANDROID,
        registeredAt = registeredAt,
        updatedAt = registeredAt,
    )

    private companion object {
        val REGISTERED_AT: OffsetDateTime = OffsetDateTime.of(2026, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC)
    }
}
