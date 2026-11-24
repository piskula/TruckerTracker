package sk.momosilabs.truckTrack.checklist.controller

import org.springframework.web.bind.annotation.RestController
import sk.momosilabs.truckTrack.api.checklist.ChecklistQuestionApi
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistQuestionDto
import sk.momosilabs.truckTrack.checklist.service.getChecklistQuestionList.GetChecklistQuestionListUseCase

@RestController
class ChecklistQuestionController(
    private val getChecklistQuestionList: GetChecklistQuestionListUseCase,
) : ChecklistQuestionApi {

    override fun getChecklistQuestionList(acceptLanguage: String?): List<ChecklistQuestionDto> =
        getChecklistQuestionList.get(acceptLanguage ?: GetChecklistQuestionListUseCase.DEFAULT_LANGUAGE)
            .map { it.toDto() }
}
