package sk.momosilabs.truckTrack.checklist.model

import java.time.OffsetDateTime

data class ChecklistQuestionModel(
    val id: Long,
    val identifier: String,
    val textJson: String,
    val position: Int,
    val createdAt: OffsetDateTime,
)
