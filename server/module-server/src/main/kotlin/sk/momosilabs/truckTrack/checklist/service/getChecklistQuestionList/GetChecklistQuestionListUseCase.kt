package sk.momosilabs.truckTrack.checklist.service.getChecklistQuestionList

import sk.momosilabs.truckTrack.checklist.model.ResolvedChecklistQuestionModel

interface GetChecklistQuestionListUseCase {

    companion object {
        const val DEFAULT_LANGUAGE = "sk"
    }

    fun get(language: String): List<ResolvedChecklistQuestionModel>
}
