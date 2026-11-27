package sk.momosilabs.truckTrack.account.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.AdditionalAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.account.entity.AccountRole
import sk.momosilabs.truckTrack.account.entity.AccountRoleEntity
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRoleRepository
import java.util.Optional
import java.util.UUID

class AccountPersistenceHelperTest {

    private val accountRepository = mock(AccountRepository::class.java)
    private val accountRoleRepository = mock(AccountRoleRepository::class.java)
    private val helper = AccountPersistenceHelper(accountRepository, accountRoleRepository)

    private val account = AccountModel(id = ACCOUNT_ID, username = "driver", firstName = "Dan", lastName = "Driver")
    private val accountEntity = AccountEntity(id = ACCOUNT_ID, username = "driver", firstName = "Dan", lastName = "Driver")

    @Test
    fun `a new account is stored with every role from the token`() {
        `when`(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty())
        `when`(accountRepository.save(any(AccountEntity::class.java))).thenAnswer(AdditionalAnswers.returnsFirstArg<AccountEntity>())
        `when`(accountRoleRepository.findAllByAccountId(ACCOUNT_ID)).thenReturn(emptyList())

        helper.sync(account, setOf(AccountRole.DRIVER, AccountRole.MECHANIC))

        assertThat(savedRoles()).containsExactlyInAnyOrder(AccountRole.DRIVER, AccountRole.MECHANIC)
        assertThat(deletedRoles()).isEmpty()
    }

    @Test
    fun `changed roles replace only the roles that differ`() {
        `when`(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity))
        `when`(accountRoleRepository.findAllByAccountId(ACCOUNT_ID)).thenReturn(
            listOf(AccountRoleEntity(id = 1, account = accountEntity, role = AccountRole.MECHANIC)),
        )

        helper.sync(account, setOf(AccountRole.DRIVER))

        assertThat(savedRoles()).containsExactly(AccountRole.DRIVER)
        assertThat(deletedRoles()).containsExactly(AccountRole.MECHANIC)
    }

    @Test
    fun `unchanged roles write nothing`() {
        `when`(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity))
        `when`(accountRoleRepository.findAllByAccountId(ACCOUNT_ID)).thenReturn(
            listOf(AccountRoleEntity(id = 1, account = accountEntity, role = AccountRole.DRIVER)),
        )

        helper.sync(account, setOf(AccountRole.DRIVER))

        verify(accountRoleRepository, never()).saveAll(anyList())
        verify(accountRoleRepository, never()).deleteAll(anyList())
    }

    @Suppress("UNCHECKED_CAST")
    private fun savedRoles(): List<AccountRole> {
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<AccountRoleEntity>>
        verify(accountRoleRepository).saveAll(captor.capture())
        return captor.value.map { it.role }
    }

    @Suppress("UNCHECKED_CAST")
    private fun deletedRoles(): List<AccountRole> {
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<AccountRoleEntity>>
        verify(accountRoleRepository).deleteAll(captor.capture())
        return captor.value.map { it.role }
    }

    private companion object {
        val ACCOUNT_ID: UUID = UUID.fromString("44444444-4444-4444-4444-444444444444")
    }
}
