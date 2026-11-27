package com.momosi.trucktrack.feature.checklist.impl

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.common.network.FailureReason
import com.momosi.trucktrack.core.uilibrary.vehicle.VehiclesContent
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlin.time.Instant

@Immutable
data class ChecklistState(
    val content: ChecklistContent = ChecklistContent.Loading,
    val selectedTruck: Vehicle? = null,
    val selectedTrailer: Vehicle? = null,
    val expandedPicker: VehicleType? = null,
    val answers: ImmutableMap<String, CheckResult> = persistentMapOf(),
    val submitStatus: ChecklistSubmitStatus = ChecklistSubmitStatus.Idle,
    val isDiscardDialogVisible: Boolean = false,
) {
    private val questions: List<ChecklistQuestionItem>
        get() = (content as? ChecklistContent.Loaded)?.questions.orEmpty()

    val totalCount: Int get() = questions.size
    val answeredCount: Int get() = questions.count { it.identifier in answers }
    val problemCount: Int get() = questions.count { answers[it.identifier] == CheckResult.Problem }
    val isSubmitting: Boolean get() = submitStatus == ChecklistSubmitStatus.InProgress
    val showMissing: Boolean get() = submitStatus == ChecklistSubmitStatus.ValidationError
    val truckMissing: Boolean get() = showMissing && selectedTruck == null
    val trailerMissing: Boolean get() = showMissing && selectedTrailer == null
}

@Immutable
sealed interface ChecklistContent {
    data object Loading : ChecklistContent

    data class Error(val reason: FailureReason) : ChecklistContent

    @Immutable
    data class Loaded(val questions: ImmutableList<ChecklistQuestionItem>, val trucks: VehiclesContent.Loaded, val trailers: VehiclesContent.Loaded) : ChecklistContent
}

@Immutable
data class ChecklistQuestionItem(val identifier: String, val text: String)

enum class CheckResult { Ok, Problem }

@Immutable
sealed interface ChecklistSubmitStatus {
    data object Idle : ChecklistSubmitStatus
    data object ValidationError : ChecklistSubmitStatus
    data object InProgress : ChecklistSubmitStatus
    data class Failed(val reason: FailureReason) : ChecklistSubmitStatus
    data object AlreadySubmittedThisWeek : ChecklistSubmitStatus
    data class Submitted(val summary: ChecklistSummary) : ChecklistSubmitStatus
}

@Immutable
data class ChecklistSummary(val truckPlate: String, val trailerPlate: String, val answeredAt: Instant, val problemCount: Int, val totalCount: Int)
