package sk.momosilabs.truckTrack.checklist.controller

import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistAnswerDto
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistQuestionDto
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistResponseCreateDto
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistResponseDto
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistResponseItemDto
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistTextDto
import sk.momosilabs.truckTrack.api.checklist.dto.ChecklistVehicleSnapshotDto
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseItem
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistText
import sk.momosilabs.truckTrack.checklist.model.ResolvedChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.VehicleSnapshot
import sk.momosilabs.truckTrack.checklist.service.submitChecklistResponse.ChecklistAnswer
import sk.momosilabs.truckTrack.checklist.service.submitChecklistResponse.SubmitChecklistResponseCommand

fun ResolvedChecklistQuestionModel.toDto() = ChecklistQuestionDto(
    identifier = identifier,
    text = text,
)

fun ChecklistResponseCreateDto.toCommand() = SubmitChecklistResponseCommand(
    truckId = truckId,
    trailerId = trailerId,
    answers = answers.map { it.toModel() },
)

private fun ChecklistAnswerDto.toModel() = ChecklistAnswer(
    identifier = identifier,
    answer = answer,
)

fun ChecklistResponseModel.toDto() = ChecklistResponseDto(
    id = id,
    truck = answers.truck.toDto(),
    trailer = answers.trailer.toDto(),
    answeredBy = answeredById,
    answeredAt = answeredAt,
    items = answers.items.map { it.toDto() },
)

private fun ChecklistResponseItem.toDto() = ChecklistResponseItemDto(
    identifier = identifier,
    texts = texts.map { it.toDto() },
    answer = answer,
)

private fun ChecklistText.toDto() = ChecklistTextDto(
    language = language,
    text = text,
)

private fun VehicleSnapshot.toDto() = ChecklistVehicleSnapshotDto(
    id = id,
    licensePlate = licensePlate,
)
