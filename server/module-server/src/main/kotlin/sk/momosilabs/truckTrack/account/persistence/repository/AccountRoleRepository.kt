package sk.momosilabs.truckTrack.account.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import sk.momosilabs.truckTrack.account.entity.AccountRoleEntity
import java.util.UUID

interface AccountRoleRepository : JpaRepository<AccountRoleEntity, Long> {

    fun findAllByAccountId(accountId: UUID): List<AccountRoleEntity>
}
