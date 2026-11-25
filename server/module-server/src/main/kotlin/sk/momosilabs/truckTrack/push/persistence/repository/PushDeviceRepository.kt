package sk.momosilabs.truckTrack.push.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import sk.momosilabs.truckTrack.push.entity.PushDeviceEntity
import java.util.UUID

interface PushDeviceRepository : JpaRepository<PushDeviceEntity, Long> {

    fun findAllByAccountIdOrderByRegisteredAtUtcAsc(accountId: UUID): List<PushDeviceEntity>

    fun findAllByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String): List<PushDeviceEntity>

    fun findByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String): PushDeviceEntity?

    fun findByFirebaseInstallationId(firebaseInstallationId: String): PushDeviceEntity?

    @Modifying
    @Query(
        "delete from push_device d " +
            "where d.firebaseInstallationId = :firebaseInstallationId and d.account.id <> :accountId"
    )
    fun deleteByFirebaseInstallationIdAndAccountIdNot(firebaseInstallationId: String, accountId: UUID): Int

    @Modifying
    @Query(
        "delete from push_device d " +
            "where d.id = :id and d.firebaseInstallationId = :firebaseInstallationId"
    )
    fun deleteByIdAndFirebaseInstallationId(id: Long, firebaseInstallationId: String): Int

    @Modifying
    @Query(
        "delete from push_device d " +
            "where d.account.id = :accountId and d.sessionId = :sessionId"
    )
    fun deleteByAccountIdAndSessionId(accountId: UUID, sessionId: String): Int

    fun findAllByAccountIdOrFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String): List<PushDeviceEntity>

}
