package sk.momosilabs.truckTrack.push.service.deletePushDevice

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

class DeletePushDeviceTest {

    private val currentUserId = UUID.randomUUID()

    @Test
    fun `deleting the registered Firebase Installation ID releases the slot`() {
        val persistence = FakePushDevicePersistence(device(FIREBASE_INSTALLATION_ID))

        useCase(persistence).delete(firebaseInstallationId = FIREBASE_INSTALLATION_ID)

        assertThat(persistence.devices).isEmpty()
        assertThat(persistence.lockedAccounts).containsExactly(currentUserId)
    }

    @Test
    fun `deleting a Firebase Installation ID that is not registered leaves the device alone`() {
        val device = device(FIREBASE_INSTALLATION_ID)
        val persistence = FakePushDevicePersistence(device)

        useCase(persistence).delete(firebaseInstallationId = OTHER_FIREBASE_INSTALLATION_ID)

        assertThat(persistence.devices).containsExactly(device)
        assertThat(persistence.lockedAccounts).containsExactly(currentUserId)
    }

    private fun useCase(persistence: FakePushDevicePersistence): DeletePushDevice {
        val currentUserService = mock(CurrentUserService::class.java)
        `when`(currentUserService.currentUserId()).thenReturn(currentUserId)
        return DeletePushDevice(persistence, currentUserService)
    }

    private fun device(firebaseInstallationId: String) = PushDeviceModel(
        id = 1L,
        accountId = currentUserId,
        firebaseInstallationId = firebaseInstallationId,
        deviceName = "Pixel 8",
        platform = PushPlatform.ANDROID,
        registeredAt = REGISTERED_AT,
        updatedAt = REGISTERED_AT,
    )

    private companion object {
        const val FIREBASE_INSTALLATION_ID = "aFirebaseInstallId1"
        const val OTHER_FIREBASE_INSTALLATION_ID = "bOtherFirebaseId2"
        val REGISTERED_AT: OffsetDateTime = OffsetDateTime.of(2026, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC)
    }
}
