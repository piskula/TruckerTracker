package com.momosi.trucktrack.core.checklist

import com.momosi.trucktrack.core.checklist.model.ChecklistQuestion
import com.momosi.trucktrack.core.checklist.model.ChecklistResponse
import com.momosi.trucktrack.core.checklist.model.ChecklistStatus
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission
import kotlinx.coroutines.flow.StateFlow

interface ChecklistRepository {

    val status: StateFlow<ChecklistStatus>

    suspend fun getQuestions(): Result<List<ChecklistQuestion>>
    suspend fun submitResponse(submission: ChecklistSubmission): Result<ChecklistResponse>
    suspend fun refreshStatus(): Result<ChecklistStatus>
    fun clearStatus()
}
