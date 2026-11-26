package sk.momosilabs.truckTrack.api.checklist

import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistResponseCreateDto
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Checklist Responses")
interface ChecklistResponseApi {

    companion object {
        private const val ENDPOINT = "/api/v1/checklist-response"
    }

    @Operation(summary = "Create checklist response")
    @ApiResponse(responseCode = "409", description = "The current user already submitted a checklist this week")
    @PostMapping("$ENDPOINT/create")
    fun createChecklistResponse(@RequestBody request: ChecklistResponseCreateDto): ChecklistResponseDto
}
