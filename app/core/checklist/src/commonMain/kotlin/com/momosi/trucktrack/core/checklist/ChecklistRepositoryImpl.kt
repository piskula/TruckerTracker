package com.momosi.trucktrack.core.checklist

import com.momosi.trucktrack.core.checklist.api.ChecklistQuestionApi
import com.momosi.trucktrack.core.checklist.api.ChecklistResponseApi
import com.momosi.trucktrack.core.checklist.dto.toChecklistQuestion
import com.momosi.trucktrack.core.checklist.dto.toChecklistResponse
import com.momosi.trucktrack.core.checklist.dto.toChecklistStatus
import com.momosi.trucktrack.core.checklist.dto.toDto
import com.momosi.trucktrack.core.checklist.model.ChecklistQuestion
import com.momosi.trucktrack.core.checklist.model.ChecklistResponse
import com.momosi.trucktrack.core.checklist.model.ChecklistStatus
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission
import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.common.network.isConflict
import com.momosi.trucktrack.core.common.network.onNetworkFailure
import com.momosi.trucktrack.core.common.network.onNoConnectionFailure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "Checklist"

class ChecklistRepositoryImpl(private val checklistQuestionApi: ChecklistQuestionApi, private val checklistResponseApi: ChecklistResponseApi) : ChecklistRepository {

    private val mutableStatus = MutableStateFlow<ChecklistStatus>(ChecklistStatus.Unknown)
    override val status: StateFlow<ChecklistStatus> = mutableStatus.asStateFlow()
    private val statusGeneration = MutableStateFlow(0)

    override suspend fun getQuestions(): Result<List<ChecklistQuestion>> = runCatchingCancellable {
        checklistQuestionApi.getChecklistQuestionList().map { it.toChecklistQuestion() }
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to get checklist questions (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to get checklist questions") }

    override suspend fun submitResponse(submission: ChecklistSubmission): Result<ChecklistResponse> {
        val result = runCatchingCancellable {
            checklistResponseApi.createChecklistResponse(submission.toDto()).toChecklistResponse()
        }
            .onSuccess { setStatus(ChecklistStatus.Completed(it)) }
            .onNoConnectionFailure { Logger.w(TAG, it, "Failed to submit checklist response (offline)") }
            .onNetworkFailure { Logger.e(TAG, it, "Failed to submit checklist response") }
        if (result.exceptionOrNull().isAlreadySubmitted()) refreshStatus()
        return result
    }

    override suspend fun refreshStatus(): Result<ChecklistStatus> {
        val generation = statusGeneration.value
        val result = runCatchingCancellable {
            checklistResponseApi.getChecklistStatus().toChecklistStatus()
        }
            .onNoConnectionFailure { Logger.w(TAG, it, "Failed to get checklist status (offline)") }
            .onNetworkFailure { Logger.e(TAG, it, "Failed to get checklist status") }
        if (statusGeneration.value != generation) return Result.success(mutableStatus.value)
        mutableStatus.value = result.getOrDefault(ChecklistStatus.Unavailable)
        return result
    }

    override fun clearStatus() {
        setStatus(ChecklistStatus.Unknown)
    }

    private fun setStatus(status: ChecklistStatus) {
        statusGeneration.update { it + 1 }
        mutableStatus.value = status
    }
}

private fun Throwable?.isAlreadySubmitted(): Boolean = this is ApiException && isConflict()
