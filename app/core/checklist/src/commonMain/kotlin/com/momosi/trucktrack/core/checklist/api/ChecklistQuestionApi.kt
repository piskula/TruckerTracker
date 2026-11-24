package com.momosi.trucktrack.core.checklist.api

import com.momosi.trucktrack.shared.checklist.ChecklistQuestionDto
import de.jensklingenberg.ktorfit.http.GET

interface ChecklistQuestionApi {

    @GET("api/v1/checklist-question")
    suspend fun getChecklistQuestionList(): List<ChecklistQuestionDto>
}
