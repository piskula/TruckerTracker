package com.momosi.trucktrack.feature.checklist.impl

sealed interface ChecklistEvent {
    data object Close : ChecklistEvent

    data class ScrollToMissing(val target: MissingTarget) : ChecklistEvent
}

sealed interface MissingTarget {
    data object Vehicles : MissingTarget

    data class Check(val questionIndex: Int) : MissingTarget
}
