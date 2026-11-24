package sk.momosilabs.truckTrack.checklist.service.submitChecklistResponse

import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel

data class SubmitChecklistResponseCommand(
    val truckId: Long,
    val trailerId: Long,
    val answers: List<ChecklistAnswer>,
)

data class ChecklistAnswer(
    val identifier: String,
    val answer: Boolean,
)

interface SubmitChecklistResponseUseCase {

    fun submit(command: SubmitChecklistResponseCommand): ChecklistResponseModel
}
