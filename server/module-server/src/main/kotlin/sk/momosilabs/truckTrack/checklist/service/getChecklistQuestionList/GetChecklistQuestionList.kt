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
import java.util.Locale

@Service
class GetChecklistQuestionList(
    private val checklistPersistence: ChecklistPersistence,
    private val objectMapper: ObjectMapper,
) : GetChecklistQuestionListUseCase {

    @IsUser
    @Transactional(readOnly = true)
    override fun get(acceptLanguage: String): List<ResolvedChecklistQuestionModel> {
        val languages = try {
            Locale.LanguageRange.parse(acceptLanguage.ifBlank { GetChecklistQuestionListUseCase.DEFAULT_LANGUAGE })
        } catch (exception: IllegalArgumentException) {
            throw GlobalUnprocessableException("Invalid Accept-Language header.", cause = exception)
        }
        return checklistPersistence.findAllQuestions().map { question ->
            ResolvedChecklistQuestionModel(
                identifier = question.identifier,
                text = resolveText(question, languages),
            )
        }
    }

    private fun resolveText(question: ChecklistQuestionModel, languages: List<Locale.LanguageRange>): String {
        val texts = QuestionTextsParser.parse(question.textJson, objectMapper)
        val language = Locale.lookupTag(languages, texts.map { it.language })
        return texts.firstOrNull { it.language.equals(language, ignoreCase = true) }?.text
            ?: texts.firstOrNull { it.language == GetChecklistQuestionListUseCase.DEFAULT_LANGUAGE }?.text
            ?: texts.firstOrNull()?.text
            ?: throw GlobalUnprocessableException("Question '${question.identifier}' has no texts.")
    }
}
