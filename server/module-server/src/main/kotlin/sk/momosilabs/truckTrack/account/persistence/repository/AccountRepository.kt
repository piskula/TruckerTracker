package sk.momosilabs.truckTrack.account.persistence.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import java.util.UUID

interface AccountRepository : JpaRepository<AccountEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findLockedById(id: UUID): AccountEntity?
}
