package com.momosi.trucktrack.core.checklist

import com.momosi.trucktrack.core.checklist.api.ChecklistQuestionApi
import com.momosi.trucktrack.core.checklist.api.ChecklistResponseApi
import com.momosi.trucktrack.core.checklist.model.ChecklistStatus
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.shared.checklist.ChecklistQuestionDto
import com.momosi.trucktrack.shared.checklist.ChecklistResponseCreateDto
import com.momosi.trucktrack.shared.checklist.ChecklistResponseDto
import com.momosi.trucktrack.shared.checklist.ChecklistStateDto
import com.momosi.trucktrack.shared.checklist.ChecklistStatusDto
import com.momosi.trucktrack.shared.checklist.ChecklistVehicleSnapshotDto
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChecklistRepositoryImplTest {

    @Test
    fun aStatusRequestedBeforeSignOutIsDroppedSoItCannotLeakToTheNextUser() = runTest {
        val api = FakeResponseApi()
        val repository = ChecklistRepositoryImpl(FakeQuestionApi, api)

        val refresh = async { repository.refreshStatus() }
        runCurrent()
        repository.clearStatus()
        api.status.complete(ChecklistStatusDto(ChecklistStateDto.PENDING))
        refresh.await()

        assertEquals(ChecklistStatus.Unknown, repository.status.value)
    }

    @Test
    fun aStatusRequestedBeforeASuccessfulSubmitCannotUndoTheCompletion() = runTest {
        val api = FakeResponseApi()
        val repository = ChecklistRepositoryImpl(FakeQuestionApi, api)

        val refresh = async { repository.refreshStatus() }
        runCurrent()
        repository.submitResponse(ChecklistSubmission(truckId = 1, trailerId = 2, answers = emptyList()))
        api.status.complete(ChecklistStatusDto(ChecklistStateDto.PENDING))

        assertTrue(refresh.await().getOrNull() is ChecklistStatus.Completed)
        assertTrue(repository.status.value is ChecklistStatus.Completed)
    }

    @Test
    fun aFailedCheckForgetsLastKnownCompletionSoAStaleWeekIsNeverAssumed() = runTest {
        val api = FakeResponseApi()
        val repository = ChecklistRepositoryImpl(FakeQuestionApi, api)
        repository.submitResponse(ChecklistSubmission(truckId = 1, trailerId = 2, answers = emptyList()))

        api.status.completeExceptionally(ApiException.NoConnection)
        repository.refreshStatus()

        assertEquals(ChecklistStatus.Unavailable, repository.status.value)
    }

    private object FakeQuestionApi : ChecklistQuestionApi {
        override suspend fun getChecklistQuestionList(): List<ChecklistQuestionDto> = emptyList()
    }

    private class FakeResponseApi : ChecklistResponseApi {
        val status = CompletableDeferred<ChecklistStatusDto>()

        override suspend fun createChecklistResponse(body: ChecklistResponseCreateDto): ChecklistResponseDto = RESPONSE

        override suspend fun getChecklistStatus(): ChecklistStatusDto = status.await()
    }

    @OptIn(ExperimentalUuidApi::class)
    private companion object {
        val RESPONSE = ChecklistResponseDto(
            id = 7,
            truck = ChecklistVehicleSnapshotDto(id = 1, licensePlate = "BA123AB"),
            trailer = ChecklistVehicleSnapshotDto(id = 2, licensePlate = "BA456CD"),
            answeredBy = Uuid.parse("11111111-2222-3333-4444-555555555555"),
            answeredAt = Instant.parse("2026-11-24T09:30:00Z"),
            items = emptyList(),
        )
    }
}
