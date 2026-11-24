package com.momosi.trucktrack.core.uilibrary.illustrations

import com.momosi.trucktrack.core.uilibrary.resources.Res
import com.momosi.trucktrack.core.uilibrary.resources.error_generic_illustration
import com.momosi.trucktrack.core.uilibrary.resources.error_no_connection_illustration
import org.jetbrains.compose.resources.DrawableResource

data object TruckTrackIllustrations {
    val NoConnection: DrawableResource = Res.drawable.error_no_connection_illustration
    val GenericError: DrawableResource = Res.drawable.error_generic_illustration
}
