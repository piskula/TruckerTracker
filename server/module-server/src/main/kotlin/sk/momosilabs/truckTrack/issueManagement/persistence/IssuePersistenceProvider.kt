package sk.momosilabs.truckTrack.issueManagement.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import sk.momosilabs.truckTrack.config.GlobalNotFoundException
import sk.momosilabs.truckTrack.issueManagement.entity.IssueEntity
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEntity
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import java.time.OffsetDateTime
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import sk.momosilabs.truckTrack.issueManagement.persistence.mapper.toDetailsJson
import sk.momosilabs.truckTrack.issueManagement.persistence.mapper.toEventType
import sk.momosilabs.truckTrack.issueManagement.persistence.mapper.toModel
import sk.momosilabs.truckTrack.issueManagement.entity.RepairType
import sk.momosilabs.truckTrack.issueManagement.entity.VehicleSystem
import sk.momosilabs.truckTrack.issueManagement.persistence.repository.IssueHistoryRepository
import sk.momosilabs.truckTrack.issueManagement.persistence.repository.IssueRepository
import sk.momosilabs.truckTrack.issueManagement.service.IssuePersistence
import sk.momosilabs.truckTrack.issueManagement.service.IssueListFilter
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime
import sk.momosilabs.truckTrack.vehicle.entity.VehicleEntity
import sk.momosilabs.truckTrack.vehicle.persistence.repository.VehicleRepository
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Repository
class IssuePersistenceProvider(
    private val issueRepository: IssueRepository,
    private val issueHistoryRepository: IssueHistoryRepository,
    private val vehicleRepository: VehicleRepository,
    private val accountRepository: AccountRepository,
    private val objectMapper: ObjectMapper,
) : IssuePersistence {

    @Transactional(readOnly = true)
    override fun findPage(filter: IssueListFilter, pageable: Pageable): Page<IssueModel> {
        var spec = Specification<IssueEntity> { _, _, cb -> cb.conjunction() }
        if (filter.statuses.isNotEmpty())
            spec = spec.and { root, _, _ ->
                root.get<IssueStatus>("status").`in`(filter.statuses)
            }
        if (filter.vehicleIds.isNotEmpty())
            spec = spec.and { root, _, _ ->
                root.get<VehicleEntity>("vehicle").get<Long>("id").`in`(filter.vehicleIds)
            }
        if (filter.accountIds.isNotEmpty())
            spec = spec.and { root, _, cb ->
                val reportedBy = root.get<AccountEntity>("reportedBy").get<UUID>("id")
                val assignedTo = root.get<AccountEntity>("assignedTo").get<UUID>("id")

                return@and cb.or(
                    reportedBy.`in`(filter.accountIds),
                    assignedTo.`in`(filter.accountIds)
                )
            }
        return issueRepository.findAll(spec, pageable).map { it.toModel() }
    }

    @Transactional(readOnly = true)
    override fun findByIdOrThrow(id: Long): IssueModel =
        issueRepository.findById(id)
            .orElseThrow { GlobalNotFoundException("issue id=$id not found") }
            .toModel()

    @Transactional
    override fun create(model: IssueModel): IssueModel {
        val entityToSave = IssueEntity(
            title = model.title,
            description = model.description,
            status = model.status,
            priority = model.priority,
            vehicle = vehicleRepository.getReferenceById(model.vehicle.id),
            reportedBy = accountRepository.getReferenceById(model.reportedBy.id),
            assignedTo = model.assignedTo?.let { accountRepository.getReferenceById(it.id) },
            repairType = model.repairType,
            vehicleSystem = model.vehicleSystem,
            createdAtUtc = model.createdAt.toUtcLocalDateTime(),
            updatedAtUtc = model.updatedAt.toUtcLocalDateTime(),
        )
        return issueRepository.save(entityToSave).toModel()
    }

    @Transactional
    override fun update(model: IssueModel): IssueModel {
        val entity = issueRepository.findById(model.id)
            .orElseThrow { GlobalNotFoundException("issue id=${model.id} not found") }
        entity.updateWith(model)
        return entity.toModel()
    }

    private fun IssueEntity.updateWith(toUpdate: IssueModel) {
        title = toUpdate.title
        description = toUpdate.description
        priority = toUpdate.priority
        vehicle = vehicleRepository.getReferenceById(toUpdate.vehicle.id)
        updatedAtUtc = toUpdate.updatedAt.toUtcLocalDateTime()
    }

    @Transactional
    override fun updateStatusAndAssignee(id: Long, status: IssueStatus, newAssignee: UUID?, updatedAt: OffsetDateTime): IssueModel {
        val entity = issueRepository.findById(id)
            .orElseThrow { GlobalNotFoundException("issue id=$id not found") }
        entity.status = status
        entity.assignedTo = newAssignee?.let { accountRepository.getReferenceById(it) }
        entity.updatedAtUtc = updatedAt.toUtcLocalDateTime()
        return entity.toModel()
    }

    @Transactional
    override fun start(id: Long, mechanicId: UUID, repairType: RepairType, vehicleSystem: VehicleSystem, updatedAt: OffsetDateTime): IssueModel {
        val entity = issueRepository.findById(id)
            .orElseThrow { GlobalNotFoundException("issue id=$id not found") }
        entity.status = IssueStatus.IN_PROGRESS
        entity.assignedTo = accountRepository.getReferenceById(mechanicId)
        entity.repairType = repairType
        entity.vehicleSystem = vehicleSystem
        entity.updatedAtUtc = updatedAt.toUtcLocalDateTime()
        return entity.toModel()
    }

    @Transactional(readOnly = true)
    override fun findHistory(issueId: Long, pageable: Pageable): Page<IssueHistoryModel> =
        issueHistoryRepository.findAllByIssueId(issueId, pageable).map { it.toModel(objectMapper) }

    @Transactional
    override fun saveHistory(model: IssueHistoryModel): IssueHistoryModel {
        val entityToSave = model.toEntity()
        return issueHistoryRepository.save(entityToSave).toModel(objectMapper)
    }

    private fun IssueHistoryModel.toEntity() = IssueHistoryEntity(
        id = id,
        issue = issueRepository.getReferenceById(issueId),
        type = toEventType(),
        performedBy = accountRepository.getReferenceById(performedBy.id),
        createdAtUtc = createdAt.toUtcLocalDateTime(),
        statusFrom = (this as? IssueHistoryModel.StatusChange)?.statusFrom,
        statusTo = (this as? IssueHistoryModel.StatusChange)?.statusTo,
        details = toDetailsJson(objectMapper),
    )

}
