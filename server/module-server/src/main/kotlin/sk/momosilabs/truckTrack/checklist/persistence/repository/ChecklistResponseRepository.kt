package sk.momosilabs.truckTrack.checklist.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import sk.momosilabs.truckTrack.checklist.entity.ChecklistResponseEntity

interface ChecklistResponseRepository : JpaRepository<ChecklistResponseEntity, Long>
