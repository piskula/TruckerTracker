package com.momosi.trucktrack.feature.issues.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.issue.IssueAttachmentRepository
import com.momosi.trucktrack.feature.issues.impl.navigation.PhotoSource
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FullScreenPhotoViewModel(private val source: PhotoSource, private val issueAttachmentRepository: IssueAttachmentRepository, private val errorReporter: ErrorReporter) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    fun onAction(action: FullScreenPhotoAction) {
        Logger.i("Action:FullScreenPhoto", action.toString())
        when (action) {
            FullScreenPhotoAction.SavePhoto -> savePhoto()
        }
    }

    private fun savePhoto() {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            val bytesResult = when (val current = source) {
                is PhotoSource.Bytes -> Result.success(current.bytes)
                is PhotoSource.Attachment -> issueAttachmentRepository.downloadPhoto(current.issueId, current.attachmentId)
            }
            bytesResult
                .onSuccess {
                    FileKit.saveFile(
                        bytes = it,
                        baseName = source.fileName.substringBeforeLast('.', source.fileName),
                        extension = source.fileName.substringAfterLast('.', "jpg"),
                    )
                }
                .onFailure { errorReporter.report(it) }
            _isSaving.value = false
        }
    }
}
