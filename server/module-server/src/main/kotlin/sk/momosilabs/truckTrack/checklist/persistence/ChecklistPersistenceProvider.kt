package sk.momosilabs.truckTrack.checklist.persistence

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.entity.AccountRole
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.checklist.model.ChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistReminderModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import sk.momosilabs.truckTrack.checklist.persistence.mapper.asNewEntity
import sk.momosilabs.truckTrack.checklist.persistence.mapper.toModel
import sk.momosilabs.truckTrack.checklist.persistence.repository.ChecklistQuestionRepository
import sk.momosilabs.truckTrack.checklist.persistence.repository.ChecklistReminderRepository
import sk.momosilabs.truckTrack.checklist.persistence.repository.ChecklistResponseRepository
import sk.momosilabs.truckTrack.checklist.service.ChecklistPersistence
import sk.momosilabs.truckTrack.vehicle.persistence.repository.VehicleRepository
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.UUID

@Repository
class ChecklistPersistenceProvider(
    private val checklistQuestionRepository: ChecklistQuestionRepository,
    private val checklistResponseRepository: ChecklistResponseRepository,
    private val checklistReminderRepository: ChecklistReminderRepository,
    private val vehicleRepository: VehicleRepository,
    private val accountRepository: AccountRepository,
    private val objectMapper: ObjectMapper,
) : ChecklistPersistence {

    @Transactional(readOnly = true)
    override fun findAllQuestions(): List<ChecklistQuestionModel> =
        checklistQuestionRepository.findAllByOrderByPositionAscIdAsc().map { it.toModel() }

    @Transactional
    override fun lockAccount(accountId: UUID) {
        accountRepository.findLockedById(accountId)
    }

    @Transactional(readOnly = true)
    override fun existsResponse(answeredById: UUID, periodStartDate: LocalDate): Boolean =
        checklistResponseRepository.existsByAnsweredByIdAndPeriodStartDate(answeredById, periodStartDate)

    @Transactional
    override fun createResponse(model: ChecklistResponseModel): ChecklistResponseModel {
        val entityToSave = model.asNewEntity(
            truckResolver = { vehicleRepository.getReferenceById(it) },
            trailerResolver = { vehicleRepository.getReferenceById(it) },
            userResolver = { accountRepository.getReferenceById(it) },
            objectMapper = objectMapper,
        )
        return checklistResponseRepository.save(entityToSave).toModel(objectMapper)
    }

    @Transactional(readOnly = true)
    override fun findDriverIdsDueForReminder(periodStartDate: LocalDate, stage: ChecklistReminderStage): List<UUID> =
        checklistReminderRepository.findAccountIdsDueForReminder(
            requiredRole = AccountRole.DRIVER,
            excludedRole = AccountRole.MECHANIC,
            periodStartDate = periodStartDate,
            stage = stage,
        )

    @Transactional
    override fun createReminder(model: ChecklistReminderModel): ChecklistReminderModel {
        val entityToSave = model.asNewEntity(accountResolver = { accountRepository.getReferenceById(it) })
        return checklistReminderRepository.save(entityToSave).toModel()
    }
}
