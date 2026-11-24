package com.momosi.trucktrack.core.checklist

import com.momosi.trucktrack.core.checklist.model.ChecklistQuestion
import com.momosi.trucktrack.core.checklist.model.ChecklistResponse
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission

interface ChecklistRepository {

    suspend fun getQuestions(): Result<List<ChecklistQuestion>>
    suspend fun submitResponse(submission: ChecklistSubmission): Result<ChecklistResponse>
}
