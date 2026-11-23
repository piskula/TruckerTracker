package sk.momosilabs.truckTrack.push.service.registerPushDevice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationEventPublisher
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.push.entity.PushPlatform
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.service.FakePushDevicePersistence
import sk.momosilabs.truckTrack.push.service.PushDeviceReplacedEvent
import sk.momosilabs.truckTrack.security.CurrentUserService
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class RegisterPushDeviceTest {

    private val currentUser = AccountModel(id = UUID.randomUUID(), username = "driver", firstName = "Driver", lastName = "Test")
    private val otherUserId = UUID.randomUUID()
    private val publishedEvents = mutableListOf<Any>()

    @Test
    fun `registering a new Firebase Installation ID takes the slot over and notifies the previous device`() {
        val persistence = FakePushDevicePersistence(device(currentUser.id, OLD_FIREBASE_INSTALLATION_ID))

        val registered = useCase(persistence).register(command(NEW_FIREBASE_INSTALLATION_ID, deviceName = "Pixel 8"))

        val device = persistence.devices.single()
        assertThat(device.firebaseInstallationId).isEqualTo(NEW_FIREBASE_INSTALLATION_ID)
        assertThat(device.deviceName).isEqualTo("Pixel 8")
        assertThat(device.registeredAt).isAfter(REGISTERED_AT)
        assertThat(registered).isEqualTo(device)
        assertThat(persistence.lockedAccounts).containsExactly(currentUser.id)
        assertThat(publishedEvents).containsExactly(
            PushDeviceReplacedEvent(
                previousFirebaseInstallationId = OLD_FIREBASE_INSTALLATION_ID,
                newDeviceName = "Pixel 8",
            )
        )
    }

    @Test
    fun `registering the Firebase Installation ID that already holds the slot refreshes it without notifying anyone`() {
        val persistence = FakePushDevicePersistence(device(currentUser.id, OLD_FIREBASE_INSTALLATION_ID))

        useCase(persistence).register(command(OLD_FIREBASE_INSTALLATION_ID, deviceName = "Renamed phone"))

        val device = persistence.devices.single()
        assertThat(device.deviceName).isEqualTo("Renamed phone")
        assertThat(device.platform).isEqualTo(PushPlatform.ANDROID)
        assertThat(device.registeredAt).isEqualTo(REGISTERED_AT)
        assertThat(device.updatedAt).isAfter(REGISTERED_AT)
        assertThat(publishedEvents).isEmpty()
    }

    @Test
    fun `registering a Firebase Installation ID held by another account removes it from that account`() {
        val persistence = FakePushDevicePersistence(device(otherUserId, NEW_FIREBASE_INSTALLATION_ID))

        useCase(persistence).register(command(NEW_FIREBASE_INSTALLATION_ID))

        val device = persistence.devices.single()
        assertThat(device.accountId).isEqualTo(currentUser.id)
        assertThat(device.firebaseInstallationId).isEqualTo(NEW_FIREBASE_INSTALLATION_ID)
        assertThat(persistence.findByAccountId(otherUserId)).isNull()
        assertThat(publishedEvents).isEmpty()
    }

    private fun useCase(persistence: FakePushDevicePersistence): RegisterPushDevice {
        val currentUserService = mock(CurrentUserService::class.java)
        `when`(currentUserService.currentUser()).thenReturn(currentUser)
        return RegisterPushDevice(persistence, currentUserService, ApplicationEventPublisher { publishedEvents += it })
    }

    private fun command(
        firebaseInstallationId: String,
        deviceName: String = "iPhone 15",
    ) = RegisterPushDeviceCommand(
        firebaseInstallationId = firebaseInstallationId,
        deviceName = deviceName,
        platform = PushPlatform.ANDROID,
    )

    private fun device(accountId: UUID, firebaseInstallationId: String) = PushDeviceModel(
        id = 7L,
        accountId = accountId,
        firebaseInstallationId = firebaseInstallationId,
        deviceName = "Old phone",
        platform = PushPlatform.IOS,
        registeredAt = REGISTERED_AT,
        updatedAt = REGISTERED_AT,
    )

    private companion object {
        const val OLD_FIREBASE_INSTALLATION_ID = "cOldFirebaseInstallId1"
        const val NEW_FIREBASE_INSTALLATION_ID = "dNewFirebaseInstallId2"
        val REGISTERED_AT: OffsetDateTime = OffsetDateTime.of(2026, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC)
    }
}
