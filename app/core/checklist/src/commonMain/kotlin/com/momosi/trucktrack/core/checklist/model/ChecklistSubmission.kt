package com.momosi.trucktrack.core.checklist.model

data class ChecklistSubmission(val truckId: Long, val trailerId: Long, val answers: List<ChecklistAnswer>)
