package sk.momosilabs.truckTrack.api.checklist

import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistQuestionDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Checklist Questions")
interface ChecklistQuestionApi {

    companion object {
        private const val ENDPOINT = "/api/v1/checklist-question"
    }

    @Operation(summary = "Get checklist questions")
    @GetMapping(ENDPOINT)
    fun getChecklistQuestionList(
        @RequestParam(required = false) language: String?,
    ): List<ChecklistQuestionDto>
}
