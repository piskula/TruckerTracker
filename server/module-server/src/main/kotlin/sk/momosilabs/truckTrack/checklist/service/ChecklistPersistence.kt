package sk.momosilabs.truckTrack.checklist.service

import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.checklist.model.ChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistReminderModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import java.time.LocalDate
import java.util.UUID

interface ChecklistPersistence {

    fun findAllQuestions(): List<ChecklistQuestionModel>

    fun lockAccount(accountId: UUID)

    fun existsResponse(answeredById: UUID, periodStartDate: LocalDate): Boolean

    fun createResponse(model: ChecklistResponseModel): ChecklistResponseModel

    fun findDriverIdsDueForReminder(periodStartDate: LocalDate, stage: ChecklistReminderStage): List<UUID>

    fun createReminder(model: ChecklistReminderModel): ChecklistReminderModel
}
