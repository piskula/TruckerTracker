package com.momosi.trucktrack.core.checklist

import com.momosi.trucktrack.core.checklist.api.ChecklistQuestionApi
import com.momosi.trucktrack.core.checklist.api.ChecklistResponseApi
import com.momosi.trucktrack.core.checklist.dto.toChecklistQuestion
import com.momosi.trucktrack.core.checklist.dto.toChecklistResponse
import com.momosi.trucktrack.core.checklist.dto.toDto
import com.momosi.trucktrack.core.checklist.model.ChecklistQuestion
import com.momosi.trucktrack.core.checklist.model.ChecklistResponse
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission
import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.onNetworkFailure
import com.momosi.trucktrack.core.common.network.onNoConnectionFailure

private const val TAG = "Checklist"

class ChecklistRepositoryImpl(private val checklistQuestionApi: ChecklistQuestionApi, private val checklistResponseApi: ChecklistResponseApi) : ChecklistRepository {

    override suspend fun getQuestions(): Result<List<ChecklistQuestion>> = runCatchingCancellable {
        checklistQuestionApi.getChecklistQuestionList().map { it.toChecklistQuestion() }
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to get checklist questions (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to get checklist questions") }

    override suspend fun submitResponse(submission: ChecklistSubmission): Result<ChecklistResponse> = runCatchingCancellable {
        checklistResponseApi.createChecklistResponse(submission.toDto()).toChecklistResponse()
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to submit checklist response (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to submit checklist response") }
}
