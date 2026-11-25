package sk.momosilabs.truckTrack.push.service.registerPushDevice

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationEventPublisher
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.config.GlobalBadRequestException
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
        assertThat(device.sessionId).isEqualTo(SESSION_ID)
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
    fun `registering the Firebase Installation ID that already holds the slot recreates the device without notifying anyone`() {
        val persistence = FakePushDevicePersistence(
            device(currentUser.id, OLD_FIREBASE_INSTALLATION_ID).copy(sessionId = "previous-session")
        )

        useCase(persistence).register(command(OLD_FIREBASE_INSTALLATION_ID, deviceName = "Renamed phone"))

        val device = persistence.devices.single()
        assertThat(device.deviceName).isEqualTo("Renamed phone")
        assertThat(device.platform).isEqualTo(PushPlatform.ANDROID)
        assertThat(device.registeredAt).isAfter(REGISTERED_AT)
        assertThat(device.updatedAt).isAfter(REGISTERED_AT)
        assertThat(device.sessionId).isEqualTo(SESSION_ID)
        assertThat(publishedEvents).isEmpty()
    }

    @Test
    fun `registration without an authenticated session id leaves the existing device untouched`() {
        val originalDevice = device(currentUser.id, OLD_FIREBASE_INSTALLATION_ID)
        val persistence = FakePushDevicePersistence(originalDevice)
        val currentUserService = mock(CurrentUserService::class.java)
        `when`(currentUserService.currentSessionId()).thenThrow(GlobalBadRequestException("Missing session"))
        val useCase = RegisterPushDevice(persistence, currentUserService, ApplicationEventPublisher { publishedEvents += it })

        assertThatThrownBy { useCase.register(command(NEW_FIREBASE_INSTALLATION_ID)) }
            .isInstanceOf(GlobalBadRequestException::class.java)

        assertThat(persistence.devices).containsExactly(originalDevice)
        assertThat(persistence.lockedAccounts).isEmpty()
        assertThat(publishedEvents).isEmpty()
    }

    @Test
    fun `registering a Firebase Installation ID held by another account removes it from that account`() {
        val persistence = FakePushDevicePersistence(device(otherUserId, NEW_FIREBASE_INSTALLATION_ID))

        useCase(persistence).register(command(NEW_FIREBASE_INSTALLATION_ID))

        val device = persistence.devices.single()
        assertThat(device.accountId).isEqualTo(currentUser.id)
        assertThat(device.firebaseInstallationId).isEqualTo(NEW_FIREBASE_INSTALLATION_ID)
        assertThat(persistence.findAllByAccountId(otherUserId)).isEmpty()
        assertThat(publishedEvents).isEmpty()
    }

    @Test
    fun `registering without a device while another account holds the Firebase Installation ID adopts it silently`() {
        val persistence = FakePushDevicePersistence(device(otherUserId, NEW_FIREBASE_INSTALLATION_ID))

        val registered = useCase(persistence).register(command(NEW_FIREBASE_INSTALLATION_ID, deviceName = "Pixel 9"))

        val device = persistence.devices.single()
        assertThat(registered).isEqualTo(device)
        assertThat(device.accountId).isEqualTo(currentUser.id)
        assertThat(device.firebaseInstallationId).isEqualTo(NEW_FIREBASE_INSTALLATION_ID)
        assertThat(device.deviceName).isEqualTo("Pixel 9")
        assertThat(device.registeredAt).isAfter(REGISTERED_AT)
        assertThat(persistence.findAllByAccountId(otherUserId)).isEmpty()
        assertThat(publishedEvents).isEmpty()
    }

    @Test
    fun `registering a new Firebase Installation ID replaces three previous devices and notifies every one of them`() {
        val persistence = FakePushDevicePersistence(
            device(currentUser.id, OLD_FIREBASE_INSTALLATION_ID),
            device(currentUser.id, FIRST_FIREBASE_INSTALLATION_ID, REGISTERED_AT.plusHours(1)),
            device(currentUser.id, SECOND_FIREBASE_INSTALLATION_ID, REGISTERED_AT.plusHours(2)),
        )

        useCase(persistence).register(command(NEW_FIREBASE_INSTALLATION_ID, deviceName = "Pixel 8"))

        assertThat(publishedEvents).containsExactlyInAnyOrder(
            PushDeviceReplacedEvent(
                previousFirebaseInstallationId = OLD_FIREBASE_INSTALLATION_ID,
                newDeviceName = "Pixel 8",
            ),
            PushDeviceReplacedEvent(
                previousFirebaseInstallationId = FIRST_FIREBASE_INSTALLATION_ID,
                newDeviceName = "Pixel 8",
            ),
            PushDeviceReplacedEvent(
                previousFirebaseInstallationId = SECOND_FIREBASE_INSTALLATION_ID,
                newDeviceName = "Pixel 8",
            ),
        )
        assertThat(publishedEvents).noneMatch {
            it is PushDeviceReplacedEvent && it.previousFirebaseInstallationId == NEW_FIREBASE_INSTALLATION_ID
        }
        val device = persistence.devices.single()
        assertThat(device.accountId).isEqualTo(currentUser.id)
        assertThat(device.firebaseInstallationId).isEqualTo(NEW_FIREBASE_INSTALLATION_ID)
    }

    @Test
    fun `registering a Firebase Installation ID that already holds one of the previous devices notifies only the other ones`() {
        val persistence = FakePushDevicePersistence(
            device(currentUser.id, OLD_FIREBASE_INSTALLATION_ID),
            device(currentUser.id, FIRST_FIREBASE_INSTALLATION_ID, REGISTERED_AT.plusHours(1)),
        )

        useCase(persistence).register(command(OLD_FIREBASE_INSTALLATION_ID, deviceName = "Renamed phone"))

        assertThat(publishedEvents).containsExactly(
            PushDeviceReplacedEvent(
                previousFirebaseInstallationId = FIRST_FIREBASE_INSTALLATION_ID,
                newDeviceName = "Renamed phone",
            )
        )
        val device = persistence.devices.single()
        assertThat(device.firebaseInstallationId).isEqualTo(OLD_FIREBASE_INSTALLATION_ID)
        assertThat(device.deviceName).isEqualTo("Renamed phone")
    }

    private fun useCase(persistence: FakePushDevicePersistence): RegisterPushDevice {
        val currentUserService = mock(CurrentUserService::class.java)
        `when`(currentUserService.currentUser()).thenReturn(currentUser)
        `when`(currentUserService.currentSessionId()).thenReturn(SESSION_ID)
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

    private fun device(
        accountId: UUID,
        firebaseInstallationId: String,
        registeredAt: OffsetDateTime = REGISTERED_AT,
    ) = PushDeviceModel(
        id = 7L,
        accountId = accountId,
        firebaseInstallationId = firebaseInstallationId,
        deviceName = "Old phone",
        platform = PushPlatform.IOS,
        registeredAt = registeredAt,
        updatedAt = registeredAt,
    )

    private companion object {
        const val SESSION_ID = "session-a"
        const val OLD_FIREBASE_INSTALLATION_ID = "cOldFirebaseInstallId1"
        const val NEW_FIREBASE_INSTALLATION_ID = "dNewFirebaseInstallId2"
        const val FIRST_FIREBASE_INSTALLATION_ID = "eFirstFirebaseInstallId4"
        const val SECOND_FIREBASE_INSTALLATION_ID = "fSecondFirebaseInstallId5"
        val REGISTERED_AT: OffsetDateTime = OffsetDateTime.of(2026, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC)
    }
}
