package com.momosi.trucktrack.feature.issues.impl.detail.startworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.issue.IssueRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StartWorkingViewModel(private val issueId: Long, private val issueRepository: IssueRepository, private val errorReporter: ErrorReporter) : ViewModel() {

    private val _state = MutableStateFlow(StartWorkingState())
    val state: StateFlow<StartWorkingState> = _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StartWorkingState(),
    )

    private val _events = Channel<StartWorkingEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: StartWorkingAction) {
        Logger.i("Action:StartWorking", action.toString())
        when (action) {
            is StartWorkingAction.SelectRepairType -> _state.update { it.copy(selectedRepairType = action.repairType) }
            is StartWorkingAction.SelectVehicleSystem -> _state.update { it.copy(selectedVehicleSystem = action.vehicleSystem) }
            is StartWorkingAction.Confirm -> confirm()
        }
    }

    private fun confirm() {
        val current = _state.value
        if (current.isSubmitting) return

        val repairType = current.selectedRepairType
        val vehicleSystem = current.selectedVehicleSystem
        if (repairType == null || vehicleSystem == null) {
            _state.update { it.copy(showValidationErrors = true) }
            return
        }

        _state.update { it.copy(isSubmitting = true, showValidationErrors = false) }
        viewModelScope.launch {
            issueRepository.startIssue(issueId, repairType, vehicleSystem)
                .onSuccess { issue ->
                    _state.update { it.copy(isSubmitting = false) }
                    _events.send(StartWorkingEvent.Started(issue))
                }
                .onFailure {
                    _state.update { state -> state.copy(isSubmitting = false) }
                    errorReporter.report(it)
                }
        }
    }
}
