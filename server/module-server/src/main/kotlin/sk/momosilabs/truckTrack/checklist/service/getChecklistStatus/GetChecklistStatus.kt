package sk.momosilabs.truckTrack.checklist.service.getChecklistStatus

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.checklist.model.ChecklistPeriod
import sk.momosilabs.truckTrack.checklist.model.ChecklistState
import sk.momosilabs.truckTrack.checklist.model.ChecklistStatusModel
import sk.momosilabs.truckTrack.checklist.service.ChecklistPersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser
import java.time.Clock
import java.time.OffsetDateTime

@Service
class GetChecklistStatus(
    private val checklistPersistence: ChecklistPersistence,
    private val currentUserService: CurrentUserService,
    private val clock: Clock,
) : GetChecklistStatusUseCase {

    @IsUser
    @Transactional(readOnly = true)
    override fun get(): ChecklistStatusModel {
        val periodStartDate = ChecklistPeriod.startDateOf(OffsetDateTime.now(clock))
        val response = checklistPersistence.findResponse(currentUserService.currentUserId(), periodStartDate)
        val state = when {
            response != null -> ChecklistState.COMPLETED
            isChecklistRequired() -> ChecklistState.PENDING
            else -> ChecklistState.NOT_REQUIRED
        }
        return ChecklistStatusModel(state = state, response = response)
    }

    private fun isChecklistRequired(): Boolean =
        currentUserService.isDriver() && !currentUserService.isMechanic()
}
