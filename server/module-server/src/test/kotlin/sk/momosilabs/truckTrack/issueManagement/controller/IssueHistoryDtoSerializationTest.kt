package sk.momosilabs.truckTrack.issueManagement.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import sk.momosilabs.truckTrack.api.issue.dto.AccountDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueHistoryDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueHistoryPageDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueStatusDto
import tools.jackson.databind.ObjectMapper
import java.time.OffsetDateTime
import java.util.UUID

@JsonTest
class IssueHistoryDtoSerializationTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `history page response includes the type discriminator for each entry`() {
        val statusChange: IssueHistoryDto = IssueHistoryDto.StatusChange(
            id = UUID.randomUUID(),
            performedBy = AccountDto(id = UUID.randomUUID(), username = "u", firstName = "F", lastName = "L"),
            createdAt = OffsetDateTime.now(),
            statusFrom = null,
            statusTo = IssueStatusDto.OPEN,
        )
        val page = IssueHistoryPageDto(
            totalElements = 1,
            totalPages = 1,
            number = 0,
            size = 500,
            numberOfElements = 1,
            content = listOf(statusChange),
        )

        val json = objectMapper.writeValueAsString(page)

        assertThat(json).contains("\"type\":\"STATUS_CHANGE\"")
    }
}
