package sk.momosilabs.truckTrack.account.persistence

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.account.entity.AccountRole
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.account.persistence.mapper.toModel
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import kotlin.jvm.optionals.getOrNull

@Component
class AccountPersistenceHelper(
    private val accountRepository: AccountRepository,
) {

    // REQUIRES_NEW: always runs in its own transaction so writes succeed even when
    // called from a read-only transaction context (e.g. GetIssueList).
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sync(account: AccountModel, role: AccountRole?) {
        val entity = accountRepository.findById(account.id).getOrNull()

        if (entity == null) {
            accountRepository.save(AccountEntity(
                id = account.id,
                username = account.username,
                firstName = account.firstName,
                lastName = account.lastName,
                role = role,
            ))
        } else if (entity.toModel() != account || entity.role != role) {
            entity.username = account.username
            entity.firstName = account.firstName
            entity.lastName = account.lastName
            entity.role = role
        }
    }
}
