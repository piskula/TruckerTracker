package sk.momosilabs.truckTrack.checklist.model

import tools.jackson.databind.ObjectMapper

object QuestionTextsParser {

    fun parse(textJson: String, objectMapper: ObjectMapper): List<ChecklistText> =
        objectMapper.readTree(textJson)
            .properties()
            .map { ChecklistText(language = it.key, text = it.value.asString()) }
}
