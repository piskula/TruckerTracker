package com.momosi.trucktrack.core.checklist.api

import com.momosi.trucktrack.shared.checklist.ChecklistResponseCreateDto
import com.momosi.trucktrack.shared.checklist.ChecklistResponseDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface ChecklistResponseApi {

    @POST("api/v1/checklist-response/create")
    suspend fun createChecklistResponse(@Body body: ChecklistResponseCreateDto): ChecklistResponseDto
}
