package sk.momosilabs.truckTrack.checklist.service.submitChecklistResponse

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.checklist.model.ChecklistPeriod
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseAnswers
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseItem
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import sk.momosilabs.truckTrack.checklist.model.QuestionTextsParser
import sk.momosilabs.truckTrack.checklist.model.VehicleSnapshot
import sk.momosilabs.truckTrack.checklist.service.ChecklistPersistence
import sk.momosilabs.truckTrack.config.GlobalConflictException
import sk.momosilabs.truckTrack.config.GlobalNotFoundException
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser
import sk.momosilabs.truckTrack.vehicle.entity.VehicleType
import sk.momosilabs.truckTrack.vehicle.service.VehiclePersistence
import tools.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.OffsetDateTime

@Service
class SubmitChecklistResponse(
    private val checklistPersistence: ChecklistPersistence,
    private val vehiclePersistence: VehiclePersistence,
    private val currentUserService: CurrentUserService,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
) : SubmitChecklistResponseUseCase {

    @IsUser
    @Transactional
    override fun submit(command: SubmitChecklistResponseCommand): ChecklistResponseModel {
        val answeredById = currentUserService.currentUser().id
        val answeredAt = OffsetDateTime.now(clock)
        val periodStartDate = ChecklistPeriod.startDateOf(answeredAt)
        checklistPersistence.lockAccount(answeredById)
        if (checklistPersistence.existsResponse(answeredById, periodStartDate)) {
            throw GlobalConflictException(ALREADY_SUBMITTED_THIS_WEEK_MESSAGE)
        }

        val questions = checklistPersistence.findAllQuestions()
        val currentIdentifiers = questions.map { it.identifier }.toSet()
        validateAnswers(command.answers, currentIdentifiers)
        val answersByIdentifier = command.answers.associate { it.identifier to it.answer }

        val truck = vehiclePersistence.findByIdOrNull(command.truckId)
            ?: throw GlobalNotFoundException("truckId=${command.truckId} not found")
        if (truck.type != VehicleType.TRUCK) {
            throw GlobalUnprocessableException("Vehicle ${command.truckId} is not a TRUCK, current type: ${truck.type}")
        }
        val trailer = vehiclePersistence.findByIdOrNull(command.trailerId)
            ?: throw GlobalNotFoundException("trailerId=${command.trailerId} not found")
        if (trailer.type != VehicleType.TRAILER) {
            throw GlobalUnprocessableException("Vehicle ${command.trailerId} is not a TRAILER, current type: ${trailer.type}")
        }

        val items = questions.map { question ->
            ChecklistResponseItem(
                identifier = question.identifier,
                texts = QuestionTextsParser.parse(question.textJson, objectMapper),
                answer = checkNotNull(answersByIdentifier[question.identifier]),
            )
        }

        return checklistPersistence.createResponse(
            ChecklistResponseModel(
                truckId = truck.id,
                trailerId = trailer.id,
                answeredById = answeredById,
                answeredAt = answeredAt,
                periodStartDate = periodStartDate,
                answers = ChecklistResponseAnswers(
                    truck = VehicleSnapshot(id = truck.id, licensePlate = truck.licensePlate),
                    trailer = VehicleSnapshot(id = trailer.id, licensePlate = trailer.licensePlate),
                    items = items,
                ),
            )
        )
    }

    private fun validateAnswers(answers: List<ChecklistAnswer>, currentIdentifiers: Set<String>) {
        if (answers.isEmpty()) {
            throw GlobalUnprocessableException("Checklist answers must not be empty.")
        }
        if (answers.filterNotNull().size != answers.size) {
            throw GlobalUnprocessableException("Checklist answers must not contain null values.")
        }
        val duplicates = answers.groupingBy { it.identifier }.eachCount().filterValues { it > 1 }.keys
        if (duplicates.isNotEmpty()) {
            throw GlobalUnprocessableException(
                "Checklist answers must not contain duplicate identifiers: ${describeIdentifiers(duplicates)}"
            )
        }
        val identifiers = answers.map { it.identifier }.toSet()
        val missing = currentIdentifiers - identifiers
        val unknown = identifiers - currentIdentifiers
        if (missing.isNotEmpty() || unknown.isNotEmpty()) {
            throw GlobalUnprocessableException(
                "Checklist answers must match current questions. missing: ${describeIdentifiers(missing)}, unknown: ${describeIdentifiers(unknown)}"
            )
        }
    }

    private fun describeIdentifiers(identifiers: Set<String>): String {
        if (identifiers.isEmpty()) {
            return "none"
        }
        val ordered = identifiers.sorted()
        val listed = ordered.take(MAX_IDENTIFIERS_LISTED).joinToString(", ") { renderIdentifier(it) }
        val hidden = ordered.size - MAX_IDENTIFIERS_LISTED
        return if (hidden > 0) "$listed (+$hidden more)" else listed
    }

    private fun renderIdentifier(identifier: String): String {
        val singleLine = identifier.replace(IDENTIFIER_WHITESPACE, " ").trim()
        return if (singleLine.length <= MAX_IDENTIFIER_DISPLAY_LENGTH) {
            singleLine
        } else {
            singleLine.take(MAX_IDENTIFIER_DISPLAY_LENGTH) + "..."
        }
    }

    private companion object {
        const val ALREADY_SUBMITTED_THIS_WEEK_MESSAGE = "Už ste to tento týždeň vyplnili."
        const val MAX_IDENTIFIERS_LISTED = 10
        const val MAX_IDENTIFIER_DISPLAY_LENGTH = 64
        val IDENTIFIER_WHITESPACE = Regex("\\s+")
    }
}
