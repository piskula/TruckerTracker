package sk.momosilabs.truckTrack.push.persistence

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import sk.momosilabs.truckTrack.config.GlobalNotFoundException
import sk.momosilabs.truckTrack.push.entity.PushDeviceEntity
import sk.momosilabs.truckTrack.push.entity.PushPlatform
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.persistence.repository.PushDeviceRepository
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class PushDevicePersistenceProviderTest {

    private val ownerAccountId = UUID.randomUUID()
    private val pushDeviceRepository = mock(PushDeviceRepository::class.java)
    private val accountRepository = mock(AccountRepository::class.java)
    private val provider = PushDevicePersistenceProvider(pushDeviceRepository, accountRepository)

    @Test
    fun `deleting by Firebase Installation ID with no matching row locks no account and deletes nothing`() {
        `when`(pushDeviceRepository.findByFirebaseInstallationId(FIREBASE_INSTALLATION_ID)).thenReturn(null)

        provider.deleteByFirebaseInstallationId(FIREBASE_INSTALLATION_ID)

        verify(pushDeviceRepository).findByFirebaseInstallationId(FIREBASE_INSTALLATION_ID)
        verify(pushDeviceRepository, never()).deleteByIdAndFirebaseInstallationId(anyLong(), anyString())
        verifyNoInteractions(accountRepository)
    }

    @Test
    fun `deleting by Firebase Installation ID locks the owner before deleting the row by its pinned id`() {
        val entity = entity(id = ENTITY_ID, firebaseInstallationId = FIREBASE_INSTALLATION_ID)
        `when`(pushDeviceRepository.findByFirebaseInstallationId(FIREBASE_INSTALLATION_ID)).thenReturn(entity)

        provider.deleteByFirebaseInstallationId(FIREBASE_INSTALLATION_ID)

        val order = inOrder(accountRepository, pushDeviceRepository)
        order.verify(accountRepository).findLockedById(ownerAccountId)
        order.verify(pushDeviceRepository).deleteByIdAndFirebaseInstallationId(ENTITY_ID, FIREBASE_INSTALLATION_ID)
    }

    @Test
    fun `locking an account delegates to the pessimistic account lookup`() {
        provider.lockAccount(ownerAccountId)

        verify(accountRepository).findLockedById(ownerAccountId)
    }

    @Test
    fun `deleting for account or Firebase Installation ID with no matching rows neither deletes nor flushes`() {
        `when`(pushDeviceRepository.findAllByAccountIdOrFirebaseInstallationId(ownerAccountId, FIREBASE_INSTALLATION_ID))
            .thenReturn(emptyList())

        provider.deleteAllForAccountOrFirebaseInstallationId(ownerAccountId, FIREBASE_INSTALLATION_ID)

        verify(pushDeviceRepository).findAllByAccountIdOrFirebaseInstallationId(ownerAccountId, FIREBASE_INSTALLATION_ID)
        verifyNoMoreInteractions(pushDeviceRepository)
    }

    @Test
    fun `deleting for account or Firebase Installation ID flushes right after deleting the matched rows`() {
        val rows = listOf(
            entity(id = 41L, firebaseInstallationId = OLD_FIREBASE_INSTALLATION_ID),
            entity(id = 42L, firebaseInstallationId = FIREBASE_INSTALLATION_ID),
        )
        `when`(pushDeviceRepository.findAllByAccountIdOrFirebaseInstallationId(ownerAccountId, FIREBASE_INSTALLATION_ID))
            .thenReturn(rows)

        provider.deleteAllForAccountOrFirebaseInstallationId(ownerAccountId, FIREBASE_INSTALLATION_ID)

        val order = inOrder(pushDeviceRepository)
        order.verify(pushDeviceRepository).deleteAll(rows)
        order.verify(pushDeviceRepository).flush()
    }

    @Test
    fun `deleting a session locks its account before deleting only the account and session match`() {
        provider.deleteByAccountIdAndSessionId(ownerAccountId, SESSION_ID)

        val order = inOrder(accountRepository, pushDeviceRepository)
        order.verify(accountRepository).findLockedById(ownerAccountId)
        order.verify(pushDeviceRepository).deleteByAccountIdAndSessionId(ownerAccountId, SESSION_ID)
        verifyNoMoreInteractions(pushDeviceRepository)
    }

    @Test
    fun `creating a push device persists its authenticated session id`() {
        val account = AccountEntity(id = ownerAccountId, username = "driver", firstName = "Driver", lastName = "Test")
        `when`(accountRepository.getReferenceById(ownerAccountId)).thenReturn(account)
        `when`(pushDeviceRepository.save(any())).thenAnswer { invocation -> invocation.getArgument<PushDeviceEntity>(0) }

        val created = provider.create(model(id = 0L, firebaseInstallationId = FIREBASE_INSTALLATION_ID))

        assertThat(created.sessionId).isEqualTo(SESSION_ID)
    }

    @Test
    fun `updating a push device that is no longer registered throws not found`() {
        `when`(pushDeviceRepository.findByAccountIdAndFirebaseInstallationId(ownerAccountId, OLD_FIREBASE_INSTALLATION_ID))
            .thenReturn(null)

        assertThatThrownBy {
            provider.update(ownerAccountId, OLD_FIREBASE_INSTALLATION_ID, NEW_FIREBASE_INSTALLATION_ID, UPDATED_AT)
        }.isInstanceOf(GlobalNotFoundException::class.java)
    }

    @Test
    fun `updating a push device swaps the Firebase Installation ID in place and keeps the registration fields`() {
        val entity = entity(id = ENTITY_ID, firebaseInstallationId = OLD_FIREBASE_INSTALLATION_ID)
        `when`(pushDeviceRepository.findByAccountIdAndFirebaseInstallationId(ownerAccountId, OLD_FIREBASE_INSTALLATION_ID))
            .thenReturn(entity)

        val updated = provider.update(ownerAccountId, OLD_FIREBASE_INSTALLATION_ID, NEW_FIREBASE_INSTALLATION_ID, UPDATED_AT)

        assertThat(updated).isEqualTo(
            PushDeviceModel(
                id = ENTITY_ID,
                accountId = ownerAccountId,
                firebaseInstallationId = NEW_FIREBASE_INSTALLATION_ID,
                deviceName = DEVICE_NAME,
                platform = PushPlatform.ANDROID,
                registeredAt = REGISTERED_AT,
                updatedAt = UPDATED_AT,
                sessionId = SESSION_ID,
            )
        )
        assertThat(entity.firebaseInstallationId).isEqualTo(NEW_FIREBASE_INSTALLATION_ID)
        assertThat(entity.updatedAtUtc).isEqualTo(UPDATED_AT.toUtcLocalDateTime())
        assertThat(entity.deviceName).isEqualTo(DEVICE_NAME)
        assertThat(entity.platform).isEqualTo(PushPlatform.ANDROID)
        assertThat(entity.registeredAtUtc).isEqualTo(REGISTERED_AT.toUtcLocalDateTime())
        verify(pushDeviceRepository, never()).save(any())
    }

    @Test
    fun `listing an account devices uses the registration-ordered query and maps every row`() {
        val older = entity(id = 41L, firebaseInstallationId = OLD_FIREBASE_INSTALLATION_ID)
        val newer = entity(id = 42L, firebaseInstallationId = NEW_FIREBASE_INSTALLATION_ID)
        `when`(pushDeviceRepository.findAllByAccountIdOrderByRegisteredAtUtcAsc(ownerAccountId)).thenReturn(listOf(older, newer))

        val devices = provider.findAllByAccountId(ownerAccountId)

        verify(pushDeviceRepository).findAllByAccountIdOrderByRegisteredAtUtcAsc(ownerAccountId)
        assertThat(devices).containsExactly(
            model(id = 41L, firebaseInstallationId = OLD_FIREBASE_INSTALLATION_ID),
            model(id = 42L, firebaseInstallationId = NEW_FIREBASE_INSTALLATION_ID),
        )
    }

    @Test
    fun `deleting by account and Firebase Installation ID removes every duplicate row sharing the Firebase Installation ID`() {
        val rows = listOf(
            entity(id = 41L, firebaseInstallationId = FIREBASE_INSTALLATION_ID),
            entity(id = 42L, firebaseInstallationId = FIREBASE_INSTALLATION_ID),
        )
        `when`(pushDeviceRepository.findAllByAccountIdAndFirebaseInstallationId(ownerAccountId, FIREBASE_INSTALLATION_ID))
            .thenReturn(rows)

        provider.deleteByAccountIdAndFirebaseInstallationId(ownerAccountId, FIREBASE_INSTALLATION_ID)

        val order = inOrder(pushDeviceRepository)
        order.verify(pushDeviceRepository).deleteAll(rows)
        order.verify(pushDeviceRepository).flush()
    }

    @Test
    fun `deleting by account and Firebase Installation ID removes only the row of that Firebase Installation ID and leaves the other one`() {
        val matching = entity(id = 41L, firebaseInstallationId = FIREBASE_INSTALLATION_ID)
        val surviving = entity(id = 42L, firebaseInstallationId = OTHER_FIREBASE_INSTALLATION_ID)
        `when`(pushDeviceRepository.findAllByAccountIdAndFirebaseInstallationId(ownerAccountId, FIREBASE_INSTALLATION_ID))
            .thenReturn(listOf(matching))
        `when`(pushDeviceRepository.findAllByAccountIdAndFirebaseInstallationId(ownerAccountId, OTHER_FIREBASE_INSTALLATION_ID))
            .thenReturn(listOf(surviving))

        provider.deleteByAccountIdAndFirebaseInstallationId(ownerAccountId, FIREBASE_INSTALLATION_ID)

        verify(pushDeviceRepository).deleteAll(listOf(matching))
        verify(pushDeviceRepository).flush()
        verify(pushDeviceRepository, never()).deleteAll(listOf(surviving))

        provider.deleteByAccountIdAndFirebaseInstallationId(ownerAccountId, OTHER_FIREBASE_INSTALLATION_ID)

        verify(pushDeviceRepository).deleteAll(listOf(surviving))
    }

    private fun entity(id: Long, firebaseInstallationId: String) = PushDeviceEntity(
        id = id,
        account = AccountEntity(id = ownerAccountId, username = "driver", firstName = "Driver", lastName = "Test"),
        firebaseInstallationId = firebaseInstallationId,
        deviceName = DEVICE_NAME,
        platform = PushPlatform.ANDROID,
        registeredAtUtc = REGISTERED_AT.toUtcLocalDateTime(),
        updatedAtUtc = REGISTERED_AT.toUtcLocalDateTime(),
        sessionId = SESSION_ID,
    )

    private fun model(id: Long, firebaseInstallationId: String) = PushDeviceModel(
        id = id,
        accountId = ownerAccountId,
        firebaseInstallationId = firebaseInstallationId,
        deviceName = DEVICE_NAME,
        platform = PushPlatform.ANDROID,
        registeredAt = REGISTERED_AT,
        updatedAt = REGISTERED_AT,
        sessionId = SESSION_ID,
    )

    private companion object {
        const val ENTITY_ID = 42L
        const val SESSION_ID = "session-a"
        const val DEVICE_NAME = "Pixel 8"
        const val FIREBASE_INSTALLATION_ID = "aFirebaseInstallId1"
        const val OLD_FIREBASE_INSTALLATION_ID = "bOldFirebaseInstallId2"
        const val NEW_FIREBASE_INSTALLATION_ID = "cNewFirebaseInstallId3"
        const val OTHER_FIREBASE_INSTALLATION_ID = "dOtherFirebaseInstallId4"
        val REGISTERED_AT: OffsetDateTime = OffsetDateTime.of(2026, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC)
        val UPDATED_AT: OffsetDateTime = OffsetDateTime.of(2026, 2, 2, 10, 30, 0, 0, ZoneOffset.UTC)
    }
}
