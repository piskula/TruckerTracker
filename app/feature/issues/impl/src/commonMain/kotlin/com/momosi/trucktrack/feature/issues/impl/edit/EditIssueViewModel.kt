package com.momosi.trucktrack.feature.issues.impl.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.coroutines.combine
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.issue.IssueCapabilityRepository
import com.momosi.trucktrack.core.issue.IssueRepository
import com.momosi.trucktrack.core.issue.model.EditingCapabilities
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.issue.model.IssueUpdate
import com.momosi.trucktrack.core.vehicle.VehicleRepository
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.feature.issues.impl.SubmitStatus
import com.momosi.trucktrack.feature.issues.impl.VehiclesContent
import com.momosi.trucktrack.user.UserRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditIssueViewModel(
    private val issueId: Long,
    private val issueRepository: IssueRepository,
    private val vehicleRepository: VehicleRepository,
    private val issueCapabilityRepository: IssueCapabilityRepository,
    private val userRepository: UserRepository,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    private val loadState = MutableStateFlow<EditIssueLoadState>(EditIssueLoadState.Loading)
    private val vehiclesContent = MutableStateFlow<VehiclesContent>(VehiclesContent.Loading)
    private val selectedVehicle = MutableStateFlow<Vehicle?>(null)
    private val vehicleDropdownExpanded = MutableStateFlow(false)
    private val title = MutableStateFlow("")
    private val description = MutableStateFlow("")
    private val selectedPriority = MutableStateFlow(IssuePriority.Medium)
    private val submitStatus = MutableStateFlow<SubmitStatus>(SubmitStatus.Idle)

    val state: StateFlow<EditIssueState> = combine(
        loadState,
        vehiclesContent,
        selectedVehicle,
        vehicleDropdownExpanded,
        title,
        description,
        selectedPriority,
        submitStatus,
    ) { loadState, vehicles, vehicle, dropdownExpanded, title, description, priority, status ->
        EditIssueState(
            loadState = loadState,
            vehicles = vehicles,
            selectedVehicle = vehicle,
            vehicleDropdownExpanded = dropdownExpanded,
            title = title,
            description = description,
            selectedPriority = priority,
            submitStatus = status,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditIssueState(),
    )

    private val _events = Channel<EditIssueEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: EditIssueAction) {
        Logger.i("Action:EditIssue", action.toString())
        when (action) {
            is EditIssueAction.SelectVehicle -> selectVehicle(action.vehicle)
            is EditIssueAction.ToggleVehicleDropdown -> vehicleDropdownExpanded.update { !it }
            is EditIssueAction.UpdateTitle -> title.value = action.title
            is EditIssueAction.UpdateDescription -> description.value = action.description
            is EditIssueAction.SelectPriority -> selectedPriority.value = action.priority
            is EditIssueAction.Submit -> submit()
            is EditIssueAction.Retry -> load()
        }
    }

    private fun load() {
        loadState.value = EditIssueLoadState.Loading
        loadVehicles()
        viewModelScope.launch {
            val issueResult = issueRepository.getIssue(issueId)
            val issue = issueResult.getOrNull()
            if (issue == null) {
                loadState.value = EditIssueLoadState.Error
                issueResult.onFailure { errorReporter.report(it) }
                return@launch
            }
            applyIssue(issue)
        }
    }

    private fun applyIssue(issue: Issue) {
        val user = userRepository.user.value
        val editing = user?.let { issueCapabilityRepository.resolve(issue, it).editing } ?: EditingCapabilities.None
        selectedVehicle.value = issue.vehicle
        title.value = issue.title
        description.value = issue.description
        selectedPriority.value = issue.priority
        loadState.value = EditIssueLoadState.Ready(editing)
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getVehicles()
                .onSuccess { vehicles -> vehiclesContent.value = VehiclesContent.Loaded(vehicles.toImmutableList()) }
                .onFailure {
                    vehiclesContent.value = VehiclesContent.Error
                    errorReporter.report(it)
                }
        }
    }

    private fun selectVehicle(vehicle: Vehicle) {
        selectedVehicle.value = vehicle
        vehicleDropdownExpanded.value = false
    }

    private fun submit() {
        if (submitStatus.value == SubmitStatus.InProgress) return

        val vehicle = selectedVehicle.value
        val currentTitle = title.value
        if (vehicle == null || currentTitle.isBlank()) {
            submitStatus.value = SubmitStatus.ValidationError
            return
        }

        submitStatus.value = SubmitStatus.InProgress

        viewModelScope.launch {
            issueRepository.updateIssue(
                issueId,
                IssueUpdate(
                    vehicleId = vehicle.id,
                    title = currentTitle,
                    description = description.value,
                    priority = selectedPriority.value,
                ),
            )
                .onSuccess { _events.send(EditIssueEvent.IssueUpdated) }
                .onFailure { submitStatus.value = SubmitStatus.RequestFailed }
        }
    }
}
