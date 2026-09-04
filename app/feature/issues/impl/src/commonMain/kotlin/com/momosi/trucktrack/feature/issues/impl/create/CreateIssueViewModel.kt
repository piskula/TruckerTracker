package com.momosi.trucktrack.feature.issues.impl.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.coroutines.combine
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.io.PhotoData
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.issue.IssueAttachmentRepository
import com.momosi.trucktrack.core.issue.IssueRepository
import com.momosi.trucktrack.core.issue.model.IssueCreate
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.vehicle.VehicleRepository
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.feature.issues.impl.SubmitStatus
import com.momosi.trucktrack.feature.issues.impl.VehiclesContent
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateIssueViewModel(private val vehicleRepository: VehicleRepository, private val issueRepository: IssueRepository, private val issueAttachmentRepository: IssueAttachmentRepository, private val errorReporter: ErrorReporter) :
    ViewModel() {

    private val vehiclesContent = MutableStateFlow<VehiclesContent>(VehiclesContent.Loading)
    private val selectedVehicle = MutableStateFlow<Vehicle?>(null)
    private val vehicleDropdownExpanded = MutableStateFlow(false)
    private val title = MutableStateFlow("")
    private val description = MutableStateFlow("")
    private val selectedPriority = MutableStateFlow(IssuePriority.Medium)
    private val photos = MutableStateFlow<ImmutableList<PhotoData>>(persistentListOf())
    private val submitStatus = MutableStateFlow<SubmitStatus>(SubmitStatus.Idle)

    val state: StateFlow<CreateIssueState> = combine(
        vehiclesContent,
        selectedVehicle,
        vehicleDropdownExpanded,
        title,
        description,
        selectedPriority,
        photos,
        submitStatus,
    ) { vehicles, vehicle, dropdownExpanded, title, description, priority, photos, status ->
        CreateIssueState(
            vehicles = vehicles,
            selectedVehicle = vehicle,
            vehicleDropdownExpanded = dropdownExpanded,
            title = title,
            description = description,
            selectedPriority = priority,
            photos = photos,
            submitStatus = status,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreateIssueState(),
    )

    private val _events = Channel<CreateIssueEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadVehicles()
    }

    fun onAction(action: CreateIssueAction) {
        Logger.i("Action:CreateIssue", action.toString())
        when (action) {
            is CreateIssueAction.SelectVehicle -> selectVehicle(action.vehicle)
            is CreateIssueAction.UpdateTitle -> title.value = action.title
            is CreateIssueAction.UpdateDescription -> description.value = action.description
            is CreateIssueAction.SelectPriority -> selectedPriority.value = action.priority
            is CreateIssueAction.AddPhotos -> addPhotos(action.files)
            is CreateIssueAction.RemovePhoto -> removePhoto(action.fileName)
            is CreateIssueAction.Submit -> submit()
            is CreateIssueAction.ToggleVehicleDropdown -> vehicleDropdownExpanded.update { !it }
        }
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getVehicles()
                .onSuccess { vehicles ->
                    vehiclesContent.value = VehiclesContent.Loaded(vehicles.toImmutableList())
                }
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

    private fun addPhotos(files: List<PlatformFile>) {
        viewModelScope.launch {
            val newPhotos = files.map { file ->
                PhotoData(
                    bytes = file.readBytes(),
                    fileName = file.name,
                    mimeType = mimeTypeFromFileName(file.name),
                )
            }
            photos.update { current ->
                (current + newPhotos).distinctBy { it.fileName }.toImmutableList()
            }
        }
    }

    private fun removePhoto(fileName: String) {
        photos.update { current -> current.filter { it.fileName != fileName }.toImmutableList() }
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
            issueRepository.createIssue(
                IssueCreate(
                    vehicleId = vehicle.id,
                    title = currentTitle,
                    description = description.value,
                    priority = selectedPriority.value,
                ),
            )
                .onSuccess { issue ->
                    uploadPhotos(issue.id, photos.value)
                    _events.send(CreateIssueEvent.IssueCreated(issue.id))
                }
                .onFailure {
                    // Inline error text (see CreateIssueScreen) already covers this; skip errorReporter to avoid a duplicate snackbar.
                    submitStatus.value = SubmitStatus.RequestFailed
                }
        }
    }

    private suspend fun uploadPhotos(issueId: Long, photos: List<PhotoData>) {
        coroutineScope {
            photos.forEach { photo ->
                launch {
                    issueAttachmentRepository.uploadPhoto(
                        issueId = issueId,
                        fileName = photo.fileName,
                        fileBytes = photo.bytes,
                        contentType = photo.mimeType,
                    )
                        .onFailure { errorReporter.report(it) }
                }
            }
        }
    }

    private fun mimeTypeFromFileName(fileName: String): String = when (
        fileName.substringAfterLast('.', "").lowercase()
    ) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "heic", "heif" -> "image/heic"
        else -> "image/jpeg"
    }
}
