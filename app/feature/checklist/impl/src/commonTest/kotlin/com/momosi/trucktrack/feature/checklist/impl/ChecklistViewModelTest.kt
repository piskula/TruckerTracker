package com.momosi.trucktrack.feature.checklist.impl

import com.momosi.trucktrack.core.checklist.ChecklistRepository
import com.momosi.trucktrack.core.checklist.model.ChecklistQuestion
import com.momosi.trucktrack.core.checklist.model.ChecklistResponse
import com.momosi.trucktrack.core.checklist.model.ChecklistResponseItem
import com.momosi.trucktrack.core.checklist.model.ChecklistStatus
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission
import com.momosi.trucktrack.core.checklist.model.ChecklistVehicle
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.common.network.FailureReason
import com.momosi.trucktrack.core.vehicle.VehicleRepository
import com.momosi.trucktrack.core.vehicle.model.CategorizedVehicles
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ChecklistViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submittingTwiceInOneWeekShowsAlreadySubmittedInsteadOfARetryableFailure() = runTest {
        val status = submitStatusAfterFailure(ApiException.HttpError(statusCode = 409, serverMessage = "Už ste to tento týždeň vyplnili."))

        assertEquals(ChecklistSubmitStatus.AlreadySubmittedThisWeek, status)
    }

    @Test
    fun otherSubmitFailuresStayRetryable() = runTest {
        assertEquals(ChecklistSubmitStatus.Failed(FailureReason.Other), submitStatusAfterFailure(ApiException.HttpError(statusCode = 500)))
        assertEquals(ChecklistSubmitStatus.Failed(FailureReason.NoConnection), submitStatusAfterFailure(ApiException.NoConnection))
    }

    @Test
    fun openingAChecklistAlreadyCompletedThisWeekShowsTheReadOnlySummaryInsteadOfTheForm() = runTest {
        val viewModel = ChecklistViewModel(FakeChecklistRepository(statusResult = Result.success(ChecklistStatus.Completed(RESPONSE))), FakeVehicleRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        assertEquals(
            ChecklistContent.AlreadyCompleted(
                ChecklistSummary(truckPlate = "BA123AB", trailerPlate = "BA456CD", answeredAt = RESPONSE.answeredAt, problemCount = 1, totalCount = 2),
            ),
            viewModel.state.value.content,
        )
    }

    @Test
    fun aFailedStatusCheckStillOpensTheForm() = runTest {
        val viewModel = ChecklistViewModel(FakeChecklistRepository(statusResult = Result.failure(ApiException.NoConnection)), FakeVehicleRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        assertTrue(viewModel.state.value.content is ChecklistContent.Loaded)
    }

    @Test
    fun completionLearnedWhileTheFormIsOpenMakesItReadOnly() = runTest {
        val repository = FakeChecklistRepository()
        val viewModel = ChecklistViewModel(repository, FakeVehicleRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        assertTrue(viewModel.state.value.content is ChecklistContent.Loaded)

        repository.status.value = ChecklistStatus.Completed(RESPONSE)

        assertTrue(viewModel.state.value.content is ChecklistContent.AlreadyCompleted)
    }

    private fun TestScope.submitStatusAfterFailure(failure: Throwable): ChecklistSubmitStatus {
        val viewModel = ChecklistViewModel(FakeChecklistRepository(submitFailure = failure), FakeVehicleRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        viewModel.onAction(ChecklistAction.Answer(identifier = QUESTION.identifier, result = CheckResult.Ok))
        viewModel.onAction(ChecklistAction.Submit)

        return viewModel.state.value.submitStatus
    }

    private class FakeChecklistRepository(private val submitFailure: Throwable = IllegalStateException(), private val statusResult: Result<ChecklistStatus> = Result.success(ChecklistStatus.Pending)) : ChecklistRepository {
        override val status = MutableStateFlow<ChecklistStatus>(ChecklistStatus.Unknown)

        override suspend fun getQuestions(): Result<List<ChecklistQuestion>> = Result.success(listOf(QUESTION))

        override suspend fun submitResponse(submission: ChecklistSubmission): Result<ChecklistResponse> = Result.failure(submitFailure)

        override suspend fun refreshStatus(): Result<ChecklistStatus> = statusResult.also { status.value = it.getOrDefault(ChecklistStatus.Unavailable) }

        override fun clearStatus() = Unit
    }

    private class FakeVehicleRepository : VehicleRepository {
        override suspend fun getVehicles(): Result<CategorizedVehicles> = Result.success(
            CategorizedVehicles(preferredTruck = TRUCK, preferredTrailer = TRAILER, otherVehicles = emptyList(), defaultVehicle = TRUCK),
        )

        override fun recordVehicleUsage(vehicle: Vehicle) = Unit
    }

    private companion object {
        val QUESTION = ChecklistQuestion(identifier = "OIL_1", text = "Kontrola hladiny oleja")
        val TRUCK = Vehicle(id = 1, licensePlate = "BA123AB", make = "Volvo", model = "FH16", type = VehicleType.Truck)
        val TRAILER = Vehicle(id = 2, licensePlate = "BA456CD", make = "Krone", model = "Profi Liner", type = VehicleType.Trailer)
        val RESPONSE = ChecklistResponse(
            id = 7,
            truck = ChecklistVehicle(id = 1, licensePlate = "BA123AB"),
            trailer = ChecklistVehicle(id = 2, licensePlate = "BA456CD"),
            answeredBy = "11111111-2222-3333-4444-555555555555",
            answeredAt = Instant.parse("2026-11-24T09:30:00Z"),
            items = listOf(
                ChecklistResponseItem(identifier = "OIL_1", texts = emptyList(), answer = true),
                ChecklistResponseItem(identifier = "WHEELS_1", texts = emptyList(), answer = false),
            ),
        )
    }
}
