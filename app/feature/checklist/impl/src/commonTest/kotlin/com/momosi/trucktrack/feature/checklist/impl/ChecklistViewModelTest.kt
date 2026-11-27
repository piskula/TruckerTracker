package com.momosi.trucktrack.feature.checklist.impl

import com.momosi.trucktrack.core.checklist.ChecklistRepository
import com.momosi.trucktrack.core.checklist.model.ChecklistQuestion
import com.momosi.trucktrack.core.checklist.model.ChecklistResponse
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.common.network.FailureReason
import com.momosi.trucktrack.core.vehicle.VehicleRepository
import com.momosi.trucktrack.core.vehicle.model.CategorizedVehicles
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private fun TestScope.submitStatusAfterFailure(failure: Throwable): ChecklistSubmitStatus {
        val viewModel = ChecklistViewModel(FailingChecklistRepository(failure), FakeVehicleRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        viewModel.onAction(ChecklistAction.Answer(identifier = QUESTION.identifier, result = CheckResult.Ok))
        viewModel.onAction(ChecklistAction.Submit)

        return viewModel.state.value.submitStatus
    }

    private class FailingChecklistRepository(private val failure: Throwable) : ChecklistRepository {
        override suspend fun getQuestions(): Result<List<ChecklistQuestion>> = Result.success(listOf(QUESTION))

        override suspend fun submitResponse(submission: ChecklistSubmission): Result<ChecklistResponse> = Result.failure(failure)
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
    }
}
