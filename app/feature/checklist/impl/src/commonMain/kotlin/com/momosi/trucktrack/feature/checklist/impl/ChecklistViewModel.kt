package com.momosi.trucktrack.feature.checklist.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.checklist.ChecklistRepository
import com.momosi.trucktrack.core.checklist.model.ChecklistAnswer
import com.momosi.trucktrack.core.checklist.model.ChecklistQuestion
import com.momosi.trucktrack.core.checklist.model.ChecklistResponse
import com.momosi.trucktrack.core.checklist.model.ChecklistStatus
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission
import com.momosi.trucktrack.core.common.coroutines.combine
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.common.network.isConflict
import com.momosi.trucktrack.core.common.network.toFailureReason
import com.momosi.trucktrack.core.uilibrary.vehicle.toVehiclesContent
import com.momosi.trucktrack.core.vehicle.VehicleRepository
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChecklistViewModel(private val checklistRepository: ChecklistRepository, private val vehicleRepository: VehicleRepository) : ViewModel() {

    private val formContent = MutableStateFlow<ChecklistContent>(ChecklistContent.Loading)
    private val content = formContent.combine(checklistRepository.status) { form, status ->
        if (status is ChecklistStatus.Completed) ChecklistContent.AlreadyCompleted(status.response.toSummary()) else form
    }
    private val selectedTruck = MutableStateFlow<Vehicle?>(null)
    private val selectedTrailer = MutableStateFlow<Vehicle?>(null)
    private val expandedPicker = MutableStateFlow<VehicleType?>(null)
    private val answers = MutableStateFlow<PersistentMap<String, CheckResult>>(persistentMapOf())
    private val submitStatus = MutableStateFlow<ChecklistSubmitStatus>(ChecklistSubmitStatus.Idle)
    private val isDiscardDialogVisible = MutableStateFlow(false)

    val state: StateFlow<ChecklistState> = combine(
        content,
        selectedTruck,
        selectedTrailer,
        expandedPicker,
        answers,
        submitStatus,
        isDiscardDialogVisible,
    ) { content, truck, trailer, picker, answers, status, discardDialog ->
        ChecklistState(
            content = content,
            selectedTruck = truck,
            selectedTrailer = trailer,
            expandedPicker = picker,
            answers = answers,
            submitStatus = status,
            isDiscardDialogVisible = discardDialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChecklistState(),
    )

    private val _events = Channel<ChecklistEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: ChecklistAction) {
        Logger.d("Action:Checklist", action.toString())
        when (action) {
            ChecklistAction.Retry -> load()
            is ChecklistAction.TogglePicker -> expandedPicker.update { if (it == action.type) null else action.type }
            is ChecklistAction.SelectVehicle -> selectVehicle(action.vehicle)
            is ChecklistAction.Answer -> answers.update { it.put(action.identifier, action.result) }
            ChecklistAction.Submit -> submit()
            ChecklistAction.RequestClose -> requestClose()
            ChecklistAction.ConfirmDiscard -> discard()
            ChecklistAction.DismissDiscard -> isDiscardDialogVisible.value = false
        }
    }

    private fun load() {
        formContent.value = ChecklistContent.Loading
        viewModelScope.launch { checklistRepository.refreshStatus() }
        viewModelScope.launch {
            val questionsRequest = async { checklistRepository.getQuestions() }
            val vehiclesRequest = async { vehicleRepository.getVehicles() }
            val questions = questionsRequest.await().getOrElse {
                formContent.value = ChecklistContent.Error(it.toFailureReason())
                return@launch
            }
            val vehicles = vehiclesRequest.await().getOrElse {
                formContent.value = ChecklistContent.Error(it.toFailureReason())
                return@launch
            }
            formContent.value = ChecklistContent.Loaded(
                questions = questions.toQuestionItems(),
                trucks = vehicles.toVehiclesContent(VehicleType.Truck),
                trailers = vehicles.toVehiclesContent(VehicleType.Trailer),
            )
            selectedTruck.update { it ?: vehicles.preferredTruck }
            selectedTrailer.update { it ?: vehicles.preferredTrailer }
        }
    }

    private fun selectVehicle(vehicle: Vehicle) {
        when (vehicle.type) {
            VehicleType.Truck -> selectedTruck.value = vehicle
            VehicleType.Trailer -> selectedTrailer.value = vehicle
        }
        expandedPicker.value = null
    }

    private fun submit() {
        val status = submitStatus.value
        if (status == ChecklistSubmitStatus.InProgress || status is ChecklistSubmitStatus.Submitted) return
        val questions = (formContent.value as? ChecklistContent.Loaded)?.questions ?: return
        val truck = selectedTruck.value
        val trailer = selectedTrailer.value
        val currentAnswers = answers.value
        val firstMissingCheck = questions.indexOfFirst { it.identifier !in currentAnswers }
        val missingTarget = when {
            truck == null || trailer == null -> MissingTarget.Vehicles
            firstMissingCheck >= 0 -> MissingTarget.Check(firstMissingCheck)
            else -> null
        }
        if (truck == null || trailer == null || missingTarget != null) {
            expandedPicker.value = null
            submitStatus.value = ChecklistSubmitStatus.ValidationError
            missingTarget?.let { _events.trySend(ChecklistEvent.ScrollToMissing(it)) }
            return
        }

        submitStatus.value = ChecklistSubmitStatus.InProgress
        viewModelScope.launch {
            checklistRepository.submitResponse(
                ChecklistSubmission(
                    truckId = truck.id,
                    trailerId = trailer.id,
                    answers = questions.map { ChecklistAnswer(it.identifier, currentAnswers.getValue(it.identifier) == CheckResult.Ok) },
                ),
            )
                .onSuccess { response ->
                    vehicleRepository.recordVehicleUsage(truck)
                    vehicleRepository.recordVehicleUsage(trailer)
                    submitStatus.value = ChecklistSubmitStatus.Submitted(response.toSummary())
                }
                .onFailure { cause -> submitStatus.value = cause.toSubmitFailureStatus() }
        }
    }

    private fun requestClose() {
        val hasUnsavedAnswers = answers.value.isNotEmpty() && submitStatus.value !is ChecklistSubmitStatus.Submitted
        if (hasUnsavedAnswers) {
            isDiscardDialogVisible.value = true
        } else {
            _events.trySend(ChecklistEvent.Close)
        }
    }

    private fun discard() {
        isDiscardDialogVisible.value = false
        _events.trySend(ChecklistEvent.Close)
    }
}

private fun List<ChecklistQuestion>.toQuestionItems() = map { ChecklistQuestionItem(identifier = it.identifier, text = it.text) }.toImmutableList()

private fun Throwable.toSubmitFailureStatus(): ChecklistSubmitStatus = if (this is ApiException && isConflict()) {
    ChecklistSubmitStatus.AlreadySubmittedThisWeek
} else {
    ChecklistSubmitStatus.Failed(toFailureReason())
}

private fun ChecklistResponse.toSummary() = ChecklistSummary(
    truckPlate = truck.licensePlate,
    trailerPlate = trailer.licensePlate,
    answeredAt = answeredAt,
    problemCount = items.count { !it.answer },
    totalCount = items.size,
)
