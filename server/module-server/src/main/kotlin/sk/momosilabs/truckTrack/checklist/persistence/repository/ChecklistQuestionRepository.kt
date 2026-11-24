package sk.momosilabs.truckTrack.checklist.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import sk.momosilabs.truckTrack.checklist.entity.ChecklistQuestionEntity

interface ChecklistQuestionRepository : JpaRepository<ChecklistQuestionEntity, Long> {

    fun findAllByOrderByPositionAscIdAsc(): List<ChecklistQuestionEntity>
}
