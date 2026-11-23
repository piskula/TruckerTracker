package sk.momosilabs.truckTrack.push.service.updatePushDevice

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import sk.momosilabs.truckTrack.config.GlobalConflictException
import sk.momosilabs.truckTrack.push.entity.PushPlatform
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.service.FakePushDevicePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class UpdatePushDeviceTest {

    private val currentUserId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID()

    @Test
    fun `the registered Firebase Installation ID moves the slot to the current one and takes it from another account`() {
        val persistence = FakePushDevicePersistence(
            device(id = 1L, accountId = currentUserId, firebaseInstallationId = REGISTERED_FIREBASE_INSTALLATION_ID),
            device(id = 2L, accountId = otherUserId, firebaseInstallationId = CURRENT_FIREBASE_INSTALLATION_ID),
        )

        useCase(persistence).update(
            registeredFirebaseInstallationId = REGISTERED_FIREBASE_INSTALLATION_ID,
            firebaseInstallationId = CURRENT_FIREBASE_INSTALLATION_ID,
        )

        val device = persistence.devices.single()
        assertThat(device.accountId).isEqualTo(currentUserId)
        assertThat(device.firebaseInstallationId).isEqualTo(CURRENT_FIREBASE_INSTALLATION_ID)
        assertThat(device.updatedAt).isAfter(REGISTERED_AT)
        assertThat(persistence.lockedAccounts).containsExactly(currentUserId)
    }

    @Test
    fun `a Firebase Installation ID that does not hold the slot cannot update it`() {
        val device = device(id = 1L, accountId = currentUserId, firebaseInstallationId = REGISTERED_FIREBASE_INSTALLATION_ID)
        val persistence = FakePushDevicePersistence(device)

        assertThatThrownBy {
            useCase(persistence).update(
                registeredFirebaseInstallationId = "eStaleFirebaseInstall3",
                firebaseInstallationId = CURRENT_FIREBASE_INSTALLATION_ID,
            )
        }.isInstanceOf(GlobalConflictException::class.java)
        assertThat(persistence.devices).containsExactly(device)
    }

    @Test
    fun `updating without a registered device is a conflict`() {
        val persistence = FakePushDevicePersistence()

        assertThatThrownBy {
            useCase(persistence).update(
                registeredFirebaseInstallationId = REGISTERED_FIREBASE_INSTALLATION_ID,
                firebaseInstallationId = CURRENT_FIREBASE_INSTALLATION_ID,
            )
        }.isInstanceOf(GlobalConflictException::class.java)
        assertThat(persistence.devices).isEmpty()
    }

    private fun useCase(persistence: FakePushDevicePersistence): UpdatePushDevice {
        val currentUserService = mock(CurrentUserService::class.java)
        `when`(currentUserService.currentUserId()).thenReturn(currentUserId)
        return UpdatePushDevice(persistence, currentUserService)
    }

    private fun device(id: Long, accountId: UUID, firebaseInstallationId: String) = PushDeviceModel(
        id = id,
        accountId = accountId,
        firebaseInstallationId = firebaseInstallationId,
        deviceName = "Pixel 8",
        platform = PushPlatform.ANDROID,
        registeredAt = REGISTERED_AT,
        updatedAt = REGISTERED_AT,
    )

    private companion object {
        const val REGISTERED_FIREBASE_INSTALLATION_ID = "cRegisteredFirebaseId1"
        const val CURRENT_FIREBASE_INSTALLATION_ID = "dCurrentFirebaseInsId2"
        val REGISTERED_AT: OffsetDateTime = OffsetDateTime.of(2026, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC)
    }
}
