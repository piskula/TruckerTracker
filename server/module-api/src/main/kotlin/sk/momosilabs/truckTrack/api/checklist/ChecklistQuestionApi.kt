package sk.momosilabs.truckTrack.api.checklist

import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistQuestionDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "Checklist Questions")
interface ChecklistQuestionApi {

    companion object {
        private const val ENDPOINT = "/api/v1/checklist-question"
    }

    @Operation(summary = "Get checklist questions")
    @GetMapping(ENDPOINT)
    fun getChecklistQuestionList(
        @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) acceptLanguage: String?,
    ): List<ChecklistQuestionDto>
}
