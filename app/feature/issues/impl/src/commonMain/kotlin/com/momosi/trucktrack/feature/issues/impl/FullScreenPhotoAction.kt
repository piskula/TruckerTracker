package com.momosi.trucktrack.feature.issues.impl

sealed interface FullScreenPhotoAction {
    data object SavePhoto : FullScreenPhotoAction
}
