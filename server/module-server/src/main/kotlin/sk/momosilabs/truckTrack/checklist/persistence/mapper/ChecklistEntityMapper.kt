package sk.momosilabs.truckTrack.checklist.persistence.mapper

import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.checklist.entity.ChecklistQuestionEntity
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderEntity
import sk.momosilabs.truckTrack.checklist.entity.ChecklistResponseEntity
import sk.momosilabs.truckTrack.checklist.model.ChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistReminderModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseAnswers
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime
import sk.momosilabs.truckTrack.util.toUtcOffsetDateTime
import sk.momosilabs.truckTrack.vehicle.entity.VehicleEntity
import tools.jackson.databind.ObjectMapper
import java.util.UUID

fun ChecklistQuestionEntity.toModel() = ChecklistQuestionModel(
    id = id,
    identifier = identifier,
    textJson = text,
    position = position,
    createdAt = createdAtUtc.toUtcOffsetDateTime(),
)

fun ChecklistResponseEntity.toModel(objectMapper: ObjectMapper) = ChecklistResponseModel(
    id = id,
    truckId = truck.id,
    trailerId = trailer.id,
    answeredById = answeredBy.id,
    answeredAt = answeredAtUtc.toUtcOffsetDateTime(),
    periodStartDate = periodStartDate,
    answers = objectMapper.readValue(answers, ChecklistResponseAnswers::class.java),
)

fun ChecklistResponseModel.asNewEntity(
    truckResolver: (Long) -> VehicleEntity,
    trailerResolver: (Long) -> VehicleEntity,
    userResolver: (UUID) -> AccountEntity,
    objectMapper: ObjectMapper,
) = ChecklistResponseEntity(
    truck = truckResolver(truckId),
    trailer = trailerResolver(trailerId),
    answeredBy = userResolver(answeredById),
    answeredAtUtc = answeredAt.toUtcLocalDateTime(),
    periodStartDate = periodStartDate,
    answers = objectMapper.writeValueAsString(answers),
)

fun ChecklistReminderModel.asNewEntity(accountResolver: (UUID) -> AccountEntity) = ChecklistReminderEntity(
    account = accountResolver(accountId),
    periodStartDate = periodStartDate,
    stage = stage,
    sentAtUtc = sentAt.toUtcLocalDateTime(),
)

fun ChecklistReminderEntity.toModel() = ChecklistReminderModel(
    id = id,
    accountId = account.id,
    periodStartDate = periodStartDate,
    stage = stage,
    sentAt = sentAtUtc.toUtcOffsetDateTime(),
)
