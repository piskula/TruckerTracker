package com.momosi.trucktrack.core.checklist.model

import kotlin.time.Instant

data class ChecklistResponse(val id: Long, val truck: ChecklistVehicle, val trailer: ChecklistVehicle, val answeredBy: String, val answeredAt: Instant, val items: List<ChecklistResponseItem>)

data class ChecklistVehicle(val id: Long, val licensePlate: String)

data class ChecklistResponseItem(val identifier: String, val texts: List<ChecklistText>, val answer: Boolean)

data class ChecklistText(val language: String, val text: String)
