package sk.momosilabs.truckTrack.checklist.service.getChecklistStatus

import sk.momosilabs.truckTrack.checklist.model.ChecklistStatusModel

interface GetChecklistStatusUseCase {

    fun get(): ChecklistStatusModel
}
