package sk.momosilabs.truckTrack.checklist.service.submitChecklistResponse

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.checklist.model.ChecklistPeriod
import sk.momosilabs.truckTrack.checklist.model.DailyChecklistPeriod
import sk.momosilabs.truckTrack.checklist.model.ChecklistQuestionModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistReminderModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistText
import sk.momosilabs.truckTrack.checklist.model.VehicleSnapshot
import sk.momosilabs.truckTrack.checklist.model.WeeklyChecklistPeriod
import sk.momosilabs.truckTrack.checklist.service.ChecklistPersistence
import sk.momosilabs.truckTrack.config.GlobalConflictException
import sk.momosilabs.truckTrack.config.GlobalNotFoundException
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.vehicle.entity.VehicleType
import sk.momosilabs.truckTrack.vehicle.model.VehicleModel
import sk.momosilabs.truckTrack.vehicle.service.VehiclePersistence
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class SubmitChecklistResponseTest {

    private val objectMapper = ObjectMapper()
    private val truck = VehicleModel(id = 1, licensePlate = "BA-111-AA", make = "Volvo", model = "FH16", type = VehicleType.TRUCK)
    private val trailer = VehicleModel(id = 2, licensePlate = "BA-222-BB", make = "Krone", model = "Profi", type = VehicleType.TRAILER)

    @Test
    fun `complete submission stores a response with position ordered items and full question texts`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        useCase(persistence).submit(command(answers = COMPLETE_ANSWERS))

        val created = persistence.createdResponses.single()
        assertThat(created.id).isEqualTo(0L)
        assertThat(created.truckId).isEqualTo(truck.id)
        assertThat(created.trailerId).isEqualTo(trailer.id)
        assertThat(created.answeredById).isEqualTo(USER_ID)
        assertThat(created.answeredAt).isEqualTo(OffsetDateTime.now(FIXED_CLOCK))
        assertThat(created.periodStartDate).isEqualTo(LocalDate.of(2026, 11, 23))
        assertThat(persistence.lockedAccounts).containsExactly(USER_ID)
        assertThat(objectMapper.valueToTree<JsonNode>(created.answers).path("language").isMissingNode).isTrue()
        assertThat(created.answers.truck).isEqualTo(VehicleSnapshot(id = truck.id, licensePlate = truck.licensePlate))
        assertThat(created.answers.trailer).isEqualTo(VehicleSnapshot(id = trailer.id, licensePlate = trailer.licensePlate))
        assertThat(created.answers.items.map { it.identifier }).containsExactly("brakes", "lights", "tyres")
        assertThat(created.answers.items.map { it.answer }).containsExactly(true, false, true)
        assertThat(created.answers.items.map { it.texts }).containsExactly(
            listOf(
                ChecklistText(language = "sk", text = "Brzdy"),
                ChecklistText(language = "en", text = "Brakes"),
            ),
            listOf(
                ChecklistText(language = "sk", text = "Svetla"),
                ChecklistText(language = "en", text = "Lights"),
            ),
            listOf(
                ChecklistText(language = "sk", text = "Pneumatiky"),
                ChecklistText(language = "en", text = "Tyres"),
            ),
        )
    }

    @Test
    fun `answers with an unknown identifier are rejected and nothing is stored`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        assertThatThrownBy {
            useCase(persistence).submit(command(answers = COMPLETE_ANSWERS + ChecklistAnswer(identifier = "oil-pressure", answer = true)))
        }.isInstanceOf(GlobalUnprocessableException::class.java)
            .hasMessageContaining("oil-pressure")
        assertThat(persistence.createdResponses).isEmpty()
    }

    @Test
    fun `answers missing a current identifier are rejected and name it`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        assertThatThrownBy {
            useCase(persistence).submit(command(answers = COMPLETE_ANSWERS.filterNot { it.identifier == "lights" }))
        }.isInstanceOf(GlobalUnprocessableException::class.java)
            .hasMessageContaining("lights")
        assertThat(persistence.createdResponses).isEmpty()
    }

    @Test
    fun `answers with a duplicated identifier are rejected and nothing is stored`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        assertThatThrownBy {
            useCase(persistence).submit(command(answers = COMPLETE_ANSWERS + ChecklistAnswer(identifier = "lights", answer = true)))
        }.isInstanceOf(GlobalUnprocessableException::class.java)
            .hasMessageContaining("duplicate")
            .hasMessageContaining("lights")
        assertThat(persistence.createdResponses).isEmpty()
    }

    @Test
    fun `an empty answers list is rejected`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        assertThatThrownBy {
            useCase(persistence).submit(command(answers = emptyList()))
        }.isInstanceOf(GlobalUnprocessableException::class.java)
            .hasMessageContaining("empty")
        assertThat(persistence.createdResponses).isEmpty()
    }

    @Test
    fun `answers against an empty question set are rejected as unknown`() {
        val persistence = FakeChecklistPersistence(emptyList())

        assertThatThrownBy {
            useCase(persistence).submit(command(answers = listOf(ChecklistAnswer(identifier = "brakes", answer = true))))
        }.isInstanceOf(GlobalUnprocessableException::class.java)
            .hasMessageContaining("brakes")
            .hasMessageContaining("unknown")
        assertThat(persistence.createdResponses).isEmpty()
    }

    @Test
    fun `a truck id resolving to a trailer-typed vehicle is rejected`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        assertThatThrownBy {
            useCase(persistence).submit(command(truckId = trailer.id, answers = COMPLETE_ANSWERS))
        }.isInstanceOf(GlobalUnprocessableException::class.java)
            .hasMessageContaining("is not a TRUCK")
        assertThat(persistence.createdResponses).isEmpty()
    }

    @Test
    fun `a trailer id resolving to a truck-typed vehicle is rejected`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        assertThatThrownBy {
            useCase(persistence).submit(command(trailerId = truck.id, answers = COMPLETE_ANSWERS))
        }.isInstanceOf(GlobalUnprocessableException::class.java)
            .hasMessageContaining("is not a TRAILER")
        assertThat(persistence.createdResponses).isEmpty()
    }

    @Test
    fun `an id of a vehicle that does not exist surfaces as entity not found`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        assertThatThrownBy {
            useCase(persistence).submit(command(truckId = MISSING_VEHICLE_ID, answers = COMPLETE_ANSWERS))
        }.isInstanceOf(GlobalNotFoundException::class.java)
            .hasMessageContaining("truckId=$MISSING_VEHICLE_ID not found")

        assertThatThrownBy {
            useCase(persistence).submit(command(trailerId = MISSING_VEHICLE_ID, answers = COMPLETE_ANSWERS))
        }.isInstanceOf(GlobalNotFoundException::class.java)
            .hasMessageContaining("trailerId=$MISSING_VEHICLE_ID not found")

        assertThat(persistence.createdResponses).isEmpty()
    }

    @Test
    fun `a second submission in the same week is rejected as a conflict and nothing more is stored`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)
        useCase(persistence, clockAt("2026-11-23T07:00:00Z")).submit(command(answers = COMPLETE_ANSWERS))

        assertThatThrownBy {
            useCase(persistence, clockAt("2026-11-29T22:59:00Z")).submit(command(answers = COMPLETE_ANSWERS))
        }.isInstanceOf(GlobalConflictException::class.java)
            .hasMessage("Už ste to tento týždeň vyplnili.")
        assertThat(persistence.createdResponses).hasSize(1)
    }

    @Test
    fun `the week boundary follows Bratislava time, not UTC`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        useCase(persistence, clockAt("2026-11-29T22:59:00Z")).submit(command(answers = COMPLETE_ANSWERS))
        useCase(persistence, clockAt("2026-11-29T23:00:00Z")).submit(command(answers = COMPLETE_ANSWERS))

        assertThat(persistence.createdResponses.map { it.periodStartDate })
            .containsExactly(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 30))
    }

    @Test
    fun `daily checklist rejects another submission today but allows one after Bratislava midnight`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)
        useCase(persistence, clockAt("2026-11-27T07:00:00Z"), DailyChecklistPeriod).submit(command(answers = COMPLETE_ANSWERS))

        assertThatThrownBy {
            useCase(persistence, clockAt("2026-11-27T22:59:00Z"), DailyChecklistPeriod).submit(command(answers = COMPLETE_ANSWERS))
        }.isInstanceOf(GlobalConflictException::class.java)
            .hasMessage("Už ste to dnes vyplnili.")
        assertThat(persistence.createdResponses).hasSize(1)

        useCase(persistence, clockAt("2026-11-27T23:00:00Z"), DailyChecklistPeriod).submit(command(answers = COMPLETE_ANSWERS))

        assertThat(persistence.createdResponses.map { it.periodStartDate })
            .containsExactly(LocalDate.of(2026, 11, 27), LocalDate.of(2026, 11, 28))
    }

    @Test
    fun `answers with a null value are rejected and nothing is stored`() {
        val persistence = FakeChecklistPersistence(SEED_QUESTIONS)

        assertThatThrownBy {
            useCase(persistence).submit(command(answers = answersWithNullValue()))
        }.isInstanceOf(GlobalUnprocessableException::class.java)
            .hasMessageContaining("null")
        assertThat(persistence.createdResponses).isEmpty()
    }

    private fun useCase(
        persistence: ChecklistPersistence,
        clock: Clock = FIXED_CLOCK,
        checklistPeriod: ChecklistPeriod = WeeklyChecklistPeriod,
    ): SubmitChecklistResponse {
        val currentUserService = mock(CurrentUserService::class.java)
        `when`(currentUserService.currentUser()).thenReturn(CURRENT_USER)
        return SubmitChecklistResponse(
            persistence, FakeVehiclePersistence(listOf(truck, trailer)), currentUserService, objectMapper, clock, checklistPeriod,
        )
    }

    private fun clockAt(instant: String): Clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC)

    private fun command(
        truckId: Long = truck.id,
        trailerId: Long = trailer.id,
        answers: List<ChecklistAnswer>,
    ) = SubmitChecklistResponseCommand(truckId = truckId, trailerId = trailerId, answers = answers)

    private fun answersWithNullValue(): List<ChecklistAnswer> =
        objectMapper.readValue(
            """[{"identifier":"brakes","answer":true},null,{"identifier":"tyres","answer":true}]""",
            object : TypeReference<List<ChecklistAnswer>>() {},
        )

    private class FakeChecklistPersistence(private val questions: List<ChecklistQuestionModel>) : ChecklistPersistence {
        val createdResponses = mutableListOf<ChecklistResponseModel>()
        val lockedAccounts = mutableListOf<UUID>()

        override fun findAllQuestions(): List<ChecklistQuestionModel> =
            questions.sortedWith(compareBy({ it.position }, { it.id }))

        override fun lockAccount(accountId: UUID) {
            lockedAccounts += accountId
        }

        override fun existsResponse(answeredById: UUID, periodStartDate: LocalDate): Boolean =
            createdResponses.any { it.answeredById == answeredById && it.periodStartDate == periodStartDate }

        override fun findResponse(answeredById: UUID, periodStartDate: LocalDate): ChecklistResponseModel? =
            throw UnsupportedOperationException()

        override fun createResponse(model: ChecklistResponseModel): ChecklistResponseModel =
            model.also { createdResponses += it }

        override fun findDriverIdsDueForReminder(periodStartDate: LocalDate, stage: ChecklistReminderStage): List<UUID> =
            throw UnsupportedOperationException()

        override fun createReminder(model: ChecklistReminderModel): ChecklistReminderModel =
            throw UnsupportedOperationException()
    }

    private class FakeVehiclePersistence(private val vehicles: List<VehicleModel>) : VehiclePersistence {
        override fun findAll(): List<VehicleModel> = throw UnsupportedOperationException()

        override fun findById(id: Long): VehicleModel = vehicles.find { it.id == id }
            ?: throw EntityNotFoundException("No entity found for key $id")

        override fun findByIdOrNull(id: Long): VehicleModel? = vehicles.find { it.id == id }
    }

    private companion object {
        val USER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555")
        val CURRENT_USER = AccountModel(id = USER_ID, username = "driver", firstName = "Dan", lastName = "Driver")
        val FIXED_CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-25T10:00:00Z"), ZoneOffset.UTC)
        const val MISSING_VEHICLE_ID = 999L

        val COMPLETE_ANSWERS = listOf(
            ChecklistAnswer(identifier = "brakes", answer = true),
            ChecklistAnswer(identifier = "lights", answer = false),
            ChecklistAnswer(identifier = "tyres", answer = true),
        )

        val SEED_QUESTIONS = listOf(
            ChecklistQuestionModel(
                id = 3,
                identifier = "tyres",
                textJson = """{"sk":"Pneumatiky","en":"Tyres"}""",
                position = 3,
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
            ),
            ChecklistQuestionModel(
                id = 1,
                identifier = "brakes",
                textJson = """{"sk":"Brzdy","en":"Brakes"}""",
                position = 1,
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
            ),
            ChecklistQuestionModel(
                id = 2,
                identifier = "lights",
                textJson = """{"sk":"Svetla","en":"Lights"}""",
                position = 2,
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
            ),
        )
    }
}
