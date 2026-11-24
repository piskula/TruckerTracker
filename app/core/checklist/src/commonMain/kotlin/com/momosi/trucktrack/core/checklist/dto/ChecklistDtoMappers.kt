package com.momosi.trucktrack.core.checklist.dto

import com.momosi.trucktrack.core.checklist.model.ChecklistAnswer
import com.momosi.trucktrack.core.checklist.model.ChecklistQuestion
import com.momosi.trucktrack.core.checklist.model.ChecklistResponse
import com.momosi.trucktrack.core.checklist.model.ChecklistResponseItem
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission
import com.momosi.trucktrack.core.checklist.model.ChecklistText
import com.momosi.trucktrack.core.checklist.model.ChecklistVehicle
import com.momosi.trucktrack.shared.checklist.ChecklistAnswerDto
import com.momosi.trucktrack.shared.checklist.ChecklistQuestionDto
import com.momosi.trucktrack.shared.checklist.ChecklistResponseCreateDto
import com.momosi.trucktrack.shared.checklist.ChecklistResponseDto
import com.momosi.trucktrack.shared.checklist.ChecklistResponseItemDto
import com.momosi.trucktrack.shared.checklist.ChecklistTextDto
import com.momosi.trucktrack.shared.checklist.ChecklistVehicleSnapshotDto
import kotlin.uuid.ExperimentalUuidApi

internal fun ChecklistQuestionDto.toChecklistQuestion(): ChecklistQuestion = ChecklistQuestion(
    identifier = identifier,
    text = text,
)

internal fun ChecklistSubmission.toDto(): ChecklistResponseCreateDto = ChecklistResponseCreateDto(
    truckId = truckId,
    trailerId = trailerId,
    answers = answers.map { it.toDto() },
)

private fun ChecklistAnswer.toDto(): ChecklistAnswerDto = ChecklistAnswerDto(
    identifier = identifier,
    answer = answer,
)

@OptIn(ExperimentalUuidApi::class)
internal fun ChecklistResponseDto.toChecklistResponse(): ChecklistResponse = ChecklistResponse(
    id = id,
    truck = truck.toChecklistVehicle(),
    trailer = trailer.toChecklistVehicle(),
    answeredBy = answeredBy.toString(),
    answeredAt = answeredAt,
    items = items.map { it.toChecklistResponseItem() },
)

private fun ChecklistVehicleSnapshotDto.toChecklistVehicle(): ChecklistVehicle = ChecklistVehicle(
    id = id,
    licensePlate = licensePlate,
)

private fun ChecklistResponseItemDto.toChecklistResponseItem(): ChecklistResponseItem = ChecklistResponseItem(
    identifier = identifier,
    texts = texts.map { it.toChecklistText() },
    answer = answer,
)

private fun ChecklistTextDto.toChecklistText(): ChecklistText = ChecklistText(
    language = language,
    text = text,
)
