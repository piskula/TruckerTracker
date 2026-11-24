package sk.momosilabs.truckTrack.checklist.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.AdditionalAnswers
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import sk.momosilabs.truckTrack.checklist.entity.ChecklistQuestionEntity
import sk.momosilabs.truckTrack.checklist.entity.ChecklistResponseEntity
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseAnswers
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseItem
import sk.momosilabs.truckTrack.checklist.model.ChecklistResponseModel
import sk.momosilabs.truckTrack.checklist.model.ChecklistText
import sk.momosilabs.truckTrack.checklist.model.VehicleSnapshot
import sk.momosilabs.truckTrack.checklist.persistence.repository.ChecklistQuestionRepository
import sk.momosilabs.truckTrack.checklist.persistence.repository.ChecklistResponseRepository
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime
import sk.momosilabs.truckTrack.util.toUtcOffsetDateTime
import sk.momosilabs.truckTrack.vehicle.entity.VehicleEntity
import sk.momosilabs.truckTrack.vehicle.entity.VehicleType
import sk.momosilabs.truckTrack.vehicle.persistence.repository.VehicleRepository
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class ChecklistPersistenceProviderTest {

    private val objectMapper = ObjectMapper()
    private val questionRepository = mock(ChecklistQuestionRepository::class.java)
    private val responseRepository = mock(ChecklistResponseRepository::class.java)
    private val vehicleRepository = mock(VehicleRepository::class.java)
    private val accountRepository = mock(AccountRepository::class.java)
    private val provider = ChecklistPersistenceProvider(questionRepository, responseRepository, vehicleRepository, accountRepository, objectMapper)

    private val truckEntity = VehicleEntity(id = 1, licensePlate = "BA-111-AA", make = "Volvo", model = "FH16", type = VehicleType.TRUCK)
    private val trailerEntity = VehicleEntity(id = 2, licensePlate = "BA-222-BB", make = "Krone", model = "Profi", type = VehicleType.TRAILER)
    private val answeredById = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
    private val accountEntity = AccountEntity(id = answeredById, username = "driver", firstName = "Dan", lastName = "Driver")

    @Test
    fun `createResponse serializes answers to jsonb, saves once and returns the saved model`() {
        val answeredAt = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS)
        `when`(vehicleRepository.getReferenceById(1L)).thenReturn(truckEntity)
        `when`(vehicleRepository.getReferenceById(2L)).thenReturn(trailerEntity)
        `when`(accountRepository.getReferenceById(answeredById)).thenReturn(accountEntity)
        `when`(responseRepository.save(any(ChecklistResponseEntity::class.java)))
            .thenAnswer(AdditionalAnswers.answer { entity: ChecklistResponseEntity ->
                ChecklistResponseEntity(
                    id = SAVED_ID,
                    truck = entity.truck,
                    trailer = entity.trailer,
                    answeredBy = entity.answeredBy,
                    answeredAtUtc = entity.answeredAtUtc,
                    answers = entity.answers,
                )
            })

        val created = provider.createResponse(model(answeredAt))

        val captor = ArgumentCaptor.forClass(ChecklistResponseEntity::class.java)
        verify(responseRepository, times(1)).save(captor.capture())
        val saved = captor.value
        assertThat(saved.truck).isSameAs(truckEntity)
        assertThat(saved.trailer).isSameAs(trailerEntity)
        assertThat(saved.answeredBy).isSameAs(accountEntity)
        assertThat(saved.answeredAtUtc).isEqualTo(answeredAt.toUtcLocalDateTime())
        val json = objectMapper.readTree(saved.answers)
        assertThat(json.path("language").isMissingNode).isTrue()
        assertThat(json.path("truck").path("id").asLong()).isEqualTo(1L)
        assertThat(json.path("truck").path("licensePlate").asString()).isEqualTo("BA-111-AA")
        assertThat(json.path("trailer").path("id").asLong()).isEqualTo(2L)
        assertThat(json.path("trailer").path("licensePlate").asString()).isEqualTo("BA-222-BB")
        assertThat(json.path("items").isArray).isTrue()
        assertThat(json.path("items").size()).isEqualTo(2)
        assertThat(json.path("items").get(0).path("identifier").asString()).isEqualTo("brakes")
        assertThat(json.path("items").get(1).path("identifier").asString()).isEqualTo("lights")
        assertThat(json.path("items").get(0).path("questionTexts").isMissingNode).isTrue()
        val texts = json.path("items").get(0).path("texts")
        assertThat(texts.isArray).isTrue()
        assertThat(texts.size()).isEqualTo(2)
        assertThat(texts.get(0).path("language").asString()).isEqualTo("sk")
        assertThat(texts.get(0).path("text").asString()).isEqualTo("Brzdy")
        assertThat(texts.get(1).path("language").asString()).isEqualTo("en")
        assertThat(texts.get(1).path("text").asString()).isEqualTo("Brakes")
        assertThat(json.path("items").get(0).path("answer").asBoolean()).isTrue()
        verify(vehicleRepository).getReferenceById(1L)
        verify(vehicleRepository).getReferenceById(2L)
        verify(accountRepository).getReferenceById(answeredById)

        assertThat(created.id).isEqualTo(SAVED_ID)
        assertThat(created.truckId).isEqualTo(1L)
        assertThat(created.trailerId).isEqualTo(2L)
        assertThat(created.answeredById).isEqualTo(answeredById)
        assertThat(created.answeredAt).isEqualTo(answeredAt.toUtcLocalDateTime().toUtcOffsetDateTime())
        assertThat(created.answers).isEqualTo(answers())
    }

    @Test
    fun `findAllQuestions delegates to the position ordered query and maps entities`() {
        val firstCreatedAt = LocalDateTime.of(2026, 11, 1, 8, 30, 0)
        val secondCreatedAt = LocalDateTime.of(2026, 11, 2, 9, 15, 0)
        `when`(questionRepository.findAllByOrderByPositionAscIdAsc()).thenReturn(
            listOf(
                ChecklistQuestionEntity(id = 1, identifier = "brakes", text = """{"sk":"Brzdy","en":"Brakes"}""", position = 1, createdAtUtc = firstCreatedAt),
                ChecklistQuestionEntity(id = 2, identifier = "lights", text = """{"sk":"Svetla","en":"Lights"}""", position = 2, createdAtUtc = secondCreatedAt),
            ),
        )

        val questions = provider.findAllQuestions()

        verify(questionRepository, times(1)).findAllByOrderByPositionAscIdAsc()
        assertThat(questions).hasSize(2)
        assertThat(questions[0].id).isEqualTo(1L)
        assertThat(questions[0].identifier).isEqualTo("brakes")
        assertThat(questions[0].textJson).isEqualTo("""{"sk":"Brzdy","en":"Brakes"}""")
        assertThat(questions[0].position).isEqualTo(1)
        assertThat(questions[0].createdAt).isEqualTo(firstCreatedAt.toUtcOffsetDateTime())
        assertThat(questions[1].identifier).isEqualTo("lights")
        assertThat(questions[1].position).isEqualTo(2)
        assertThat(questions[1].createdAt).isEqualTo(secondCreatedAt.toUtcOffsetDateTime())
    }

    private fun model(answeredAt: OffsetDateTime) = ChecklistResponseModel(
        truckId = 1,
        trailerId = 2,
        answeredById = answeredById,
        answeredAt = answeredAt,
        answers = answers(),
    )

    private fun answers() = ChecklistResponseAnswers(
        truck = VehicleSnapshot(id = 1, licensePlate = "BA-111-AA"),
        trailer = VehicleSnapshot(id = 2, licensePlate = "BA-222-BB"),
        items = listOf(
            ChecklistResponseItem(
                identifier = "brakes",
                texts = listOf(
                    ChecklistText(language = "sk", text = "Brzdy"),
                    ChecklistText(language = "en", text = "Brakes"),
                ),
                answer = true,
            ),
            ChecklistResponseItem(
                identifier = "lights",
                texts = listOf(
                    ChecklistText(language = "sk", text = "Svetla"),
                    ChecklistText(language = "en", text = "Lights"),
                ),
                answer = false,
            ),
        ),
    )

    private companion object {
        const val SAVED_ID = 42L
    }
}
