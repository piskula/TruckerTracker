package sk.momosilabs.truckTrack.checklist.service

import sk.momosilabs.truckTrack.checklist.model.ChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel

interface ChecklistPersistence {

    fun findAllQuestions(): List<ChecklistQuestionModel>

    fun createResponse(model: ChecklistResponseModel): ChecklistResponseModel
}
