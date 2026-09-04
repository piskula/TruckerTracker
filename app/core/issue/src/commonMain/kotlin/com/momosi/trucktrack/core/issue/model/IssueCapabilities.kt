package com.momosi.trucktrack.core.issue.model

data class IssueCapabilities(val editing: EditingCapabilities, val actions: ActionCapabilities) {
    companion object {
        val None = IssueCapabilities(editing = EditingCapabilities.None, actions = ActionCapabilities.None)
    }
}

data class EditingCapabilities(val canEditTitle: Boolean, val canEditDescription: Boolean, val canEditPriority: Boolean, val canEditVehicle: Boolean) {
    val canEditAny: Boolean
        get() = canEditTitle || canEditDescription || canEditPriority || canEditVehicle

    companion object {
        val None = EditingCapabilities(
            canEditTitle = false,
            canEditDescription = false,
            canEditPriority = false,
            canEditVehicle = false,
        )
    }
}

data class ActionCapabilities(val nextStateAction: IssueStateAction?, val canDeletePhotos: Boolean) {
    companion object {
        val None = ActionCapabilities(nextStateAction = null, canDeletePhotos = false)
    }
}
