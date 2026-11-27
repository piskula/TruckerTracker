package sk.momosilabs.truckTrack.account.persistence

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.account.entity.AccountRole
import sk.momosilabs.truckTrack.account.entity.AccountRoleEntity
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.account.persistence.mapper.toModel
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRoleRepository
import kotlin.jvm.optionals.getOrNull

@Component
class AccountPersistenceHelper(
    private val accountRepository: AccountRepository,
    private val accountRoleRepository: AccountRoleRepository,
) {

    // REQUIRES_NEW: always runs in its own transaction so writes succeed even when
    // called from a read-only transaction context (e.g. GetIssueList).
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sync(account: AccountModel, roles: Set<AccountRole>) {
        val entity = syncAccount(account)
        syncRoles(entity, roles)
    }

    private fun syncAccount(account: AccountModel): AccountEntity {
        val entity = accountRepository.findById(account.id).getOrNull()
            ?: return accountRepository.save(AccountEntity(
                id = account.id,
                username = account.username,
                firstName = account.firstName,
                lastName = account.lastName,
            ))
        if (entity.toModel() != account) {
            entity.username = account.username
            entity.firstName = account.firstName
            entity.lastName = account.lastName
        }
        return entity
    }

    private fun syncRoles(account: AccountEntity, roles: Set<AccountRole>) {
        val stored = accountRoleRepository.findAllByAccountId(account.id)
        val storedRoles = stored.mapTo(HashSet()) { it.role }
        if (storedRoles == roles) {
            return
        }
        accountRoleRepository.deleteAll(stored.filter { it.role !in roles })
        accountRoleRepository.saveAll((roles - storedRoles).map { AccountRoleEntity(account = account, role = it) })
    }
}
