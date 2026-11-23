package sk.momosilabs.truckTrack.push.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import sk.momosilabs.truckTrack.push.entity.PushDeviceEntity
import java.util.UUID

interface PushDeviceRepository : JpaRepository<PushDeviceEntity, Long> {

    fun findByAccountId(accountId: UUID): PushDeviceEntity?

    @Modifying
    @Query(
        "delete from push_device d " +
            "where d.firebaseInstallationId = :firebaseInstallationId and d.account.id <> :accountId"
    )
    fun deleteByFirebaseInstallationIdAndAccountIdNot(firebaseInstallationId: String, accountId: UUID): Int

    @Modifying
    @Query(
        "delete from push_device d " +
            "where d.account.id = :accountId and d.firebaseInstallationId = :firebaseInstallationId"
    )
    fun deleteByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String): Int

    @Modifying
    @Query("delete from push_device d where d.firebaseInstallationId = :firebaseInstallationId")
    fun deleteByFirebaseInstallationId(firebaseInstallationId: String): Int
}
