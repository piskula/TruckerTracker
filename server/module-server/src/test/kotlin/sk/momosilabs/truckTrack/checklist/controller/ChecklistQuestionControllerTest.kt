package sk.momosilabs.truckTrack.checklist.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import sk.momosilabs.truckTrack.checklist.model.ResolvedChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.service.getChecklistQuestionList.GetChecklistQuestionListUseCase

class ChecklistQuestionControllerTest {

    @Test
    fun `language comes from the header and not the removed query parameter`() {
        val useCase = RecordingGetChecklistQuestionList()
        val mvc = MockMvcBuilders.standaloneSetup(ChecklistQuestionController(useCase)).build()

        mvc.perform(
            get("/api/v1/checklist-question")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9")
                .param("language", "sk")
        ).andExpect(status().isOk)

        assertThat(useCase.acceptLanguage).isEqualTo("en-US,en;q=0.9")
    }

    @Test
    fun `missing header uses Slovak even when the removed query parameter is present`() {
        val useCase = RecordingGetChecklistQuestionList()
        val mvc = MockMvcBuilders.standaloneSetup(ChecklistQuestionController(useCase)).build()

        mvc.perform(get("/api/v1/checklist-question").param("language", "en"))
            .andExpect(status().isOk)

        assertThat(useCase.acceptLanguage).isEqualTo("sk")
    }

    private class RecordingGetChecklistQuestionList : GetChecklistQuestionListUseCase {
        var acceptLanguage: String? = null
            private set

        override fun get(acceptLanguage: String): List<ResolvedChecklistQuestionModel> {
            this.acceptLanguage = acceptLanguage
            return emptyList()
        }
    }
}
