package sk.momosilabs.truckTrack.checklist.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.ColumnTransformer
import java.time.LocalDateTime

@Entity(name = "checklist_question")
class ChecklistQuestionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @field:NotNull
    val identifier: String,

    @field:NotNull
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    val text: String,

    @field:NotNull
    val position: Int,

    @field:NotNull
    val createdAtUtc: LocalDateTime,

)
