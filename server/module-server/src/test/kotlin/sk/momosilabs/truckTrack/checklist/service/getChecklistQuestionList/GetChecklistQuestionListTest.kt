package sk.momosilabs.truckTrack.checklist.service.getChecklistQuestionList

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import sk.momosilabs.truckTrack.checklist.model.ChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import sk.momosilabs.truckTrack.checklist.service.ChecklistPersistence
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import tools.jackson.databind.ObjectMapper
import java.time.OffsetDateTime
import java.time.ZoneOffset

class GetChecklistQuestionListTest {

    private val useCase = GetChecklistQuestionList(FakeChecklistPersistence(QUESTIONS), ObjectMapper())

    @Test
    fun `the requested language is used when present`() {
        val result = useCase.get("en")

        assertThat(result).filteredOn { it.identifier == "brakes" }.singleElement()
            .extracting<String> { it.text }.isEqualTo("Brakes")
        assertThat(result).filteredOn { it.identifier == "lights" }.singleElement()
            .extracting<String> { it.text }.isEqualTo("Lights")
    }

    @Test
    fun `a requested language without entries falls back to the default language`() {
        val result = useCase.get("de")

        val brakes = result.single { it.identifier == "brakes" }
        assertThat(brakes.text).isEqualTo("Brzdy")
        assertThat(result.single { it.identifier == "lights" }.text).isEqualTo("Svetla")
        assertThat(GetChecklistQuestionListUseCase.DEFAULT_LANGUAGE).isEqualTo("sk")
    }

    @Test
    fun `a question without the default language falls back to its single entry`() {
        val result = useCase.get("en")

        assertThat(result.single { it.identifier == "coupling" }.text).isEqualTo("Kupplung")
    }

    @Test
    fun `a question without the default language and several entries falls back to the first one`() {
        val result = useCase.get("en")

        assertThat(result.single { it.identifier == "adblue" }.text).isEqualTo("AdBlue")
    }

    @Test
    fun `results keep persistence order and pass identifiers through`() {
        val result = useCase.get("en")

        assertThat(result.map { it.identifier }).containsExactly("lights", "tyres", "brakes", "coupling", "adblue")
    }

    @Test
    fun `a question with an empty texts map is rejected`() {
        val brokenUseCase = GetChecklistQuestionList(
            FakeChecklistPersistence(listOf(question(id = 6, identifier = "oil", position = 6, textJson = "{}"))),
            ObjectMapper(),
        )

        assertThatThrownBy { brokenUseCase.get("en") }
            .isInstanceOf(GlobalUnprocessableException::class.java)
            .hasMessageContaining("oil")
            .hasMessageContaining("no texts")
    }

    private class FakeChecklistPersistence(private val questions: List<ChecklistQuestionModel>) : ChecklistPersistence {
        override fun findAllQuestions(): List<ChecklistQuestionModel> = questions
        override fun createResponse(model: ChecklistResponseModel): ChecklistResponseModel = throw UnsupportedOperationException()
    }

    private companion object {
        val QUESTIONS = listOf(
            question(id = 2, identifier = "lights", position = 2, textJson = """{"sk":"Svetla","en":"Lights"}"""),
            question(id = 3, identifier = "tyres", position = 3, textJson = """{"sk":"Pneumatiky","en":"Tyres"}"""),
            question(id = 1, identifier = "brakes", position = 1, textJson = """{"sk":"Brzdy","en":"Brakes"}"""),
            question(id = 4, identifier = "coupling", position = 4, textJson = """{"de":"Kupplung"}"""),
            question(id = 5, identifier = "adblue", position = 5, textJson = """{"hu":"AdBlue","de":"AdBlue-Kontrolle"}"""),
        )

        fun question(id: Long, identifier: String, position: Int, textJson: String) = ChecklistQuestionModel(
            id = id,
            identifier = identifier,
            textJson = textJson,
            position = position,
            createdAt = OffsetDateTime.now(ZoneOffset.UTC),
        )
    }
}
