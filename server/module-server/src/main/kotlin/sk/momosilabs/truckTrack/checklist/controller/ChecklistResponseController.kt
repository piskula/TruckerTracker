package sk.momosilabs.truckTrack.checklist.controller

import org.springframework.web.bind.annotation.RestController
import sk.momosilabs.truckTrack.api.checklist.ChecklistResponseApi
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistResponseCreateDto
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistResponseDto
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistStatusDto
import sk.momosilabs.truckTrack.checklist.service.getChecklistStatus.GetChecklistStatusUseCase
import sk.momosilabs.truckTrack.checklist.service.submitChecklistResponse.SubmitChecklistResponseUseCase

@RestController
class ChecklistResponseController(
    private val submitChecklistResponse: SubmitChecklistResponseUseCase,
    private val getChecklistStatus: GetChecklistStatusUseCase,
) : ChecklistResponseApi {

    override fun createChecklistResponse(request: ChecklistResponseCreateDto): ChecklistResponseDto =
        submitChecklistResponse.submit(request.toCommand()).toDto()

    override fun getChecklistStatus(): ChecklistStatusDto =
        getChecklistStatus.get().toDto()
}
