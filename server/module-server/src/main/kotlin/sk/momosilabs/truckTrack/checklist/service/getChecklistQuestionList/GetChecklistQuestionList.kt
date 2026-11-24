package sk.momosilabs.truckTrack.checklist.service.getChecklistQuestionList

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.checklist.model.ChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.QuestionTextsParser
import sk.momosilabs.truckTrack.checklist.model.ResolvedChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.service.ChecklistPersistence
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.security.annotation.IsUser
import tools.jackson.databind.ObjectMapper

@Service
class GetChecklistQuestionList(
    private val checklistPersistence: ChecklistPersistence,
    private val objectMapper: ObjectMapper,
) : GetChecklistQuestionListUseCase {

    @IsUser
    @Transactional(readOnly = true)
    override fun get(language: String): List<ResolvedChecklistQuestionModel> =
        checklistPersistence.findAllQuestions().map { question ->
            ResolvedChecklistQuestionModel(
                identifier = question.identifier,
                text = resolveText(question, language),
            )
        }

    private fun resolveText(question: ChecklistQuestionModel, language: String): String {
        val texts = QuestionTextsParser.parse(question.textJson, objectMapper)
        return texts.firstOrNull { it.language == language }?.text
            ?: texts.firstOrNull { it.language == GetChecklistQuestionListUseCase.DEFAULT_LANGUAGE }?.text
            ?: texts.firstOrNull()?.text
            ?: throw GlobalUnprocessableException("Question '${question.identifier}' has no texts.")
    }
}
