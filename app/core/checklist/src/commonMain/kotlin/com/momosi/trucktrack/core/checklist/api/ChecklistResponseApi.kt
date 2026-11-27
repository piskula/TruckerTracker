package com.momosi.trucktrack.core.checklist.api

import com.momosi.trucktrack.shared.checklist.ChecklistResponseCreateDto
import com.momosi.trucktrack.shared.checklist.ChecklistResponseDto
import com.momosi.trucktrack.shared.checklist.ChecklistStatusDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST

interface ChecklistResponseApi {

    @POST("api/v1/checklist-response/create")
    suspend fun createChecklistResponse(@Body body: ChecklistResponseCreateDto): ChecklistResponseDto

    @GET("api/v1/checklist-response/status")
    suspend fun getChecklistStatus(): ChecklistStatusDto
}
