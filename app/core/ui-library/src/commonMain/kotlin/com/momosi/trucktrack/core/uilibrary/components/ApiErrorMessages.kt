package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.runtime.Composable
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.uilibrary.resources.Res
import com.momosi.trucktrack.core.uilibrary.resources.error_generic
import com.momosi.trucktrack.core.uilibrary.resources.error_no_connection
import com.momosi.trucktrack.core.uilibrary.resources.error_unauthorized
import org.jetbrains.compose.resources.stringResource

internal data class ApiErrorMessages(val noConnection: String, val unauthorized: String, val generic: String)

@Composable
internal fun rememberApiErrorMessages(): ApiErrorMessages = ApiErrorMessages(
    noConnection = stringResource(Res.string.error_no_connection),
    unauthorized = stringResource(Res.string.error_unauthorized),
    generic = stringResource(Res.string.error_generic),
)

internal fun Throwable.toMessage(messages: ApiErrorMessages): String = when (this) {
    is ApiException.NoConnection -> messages.noConnection
    is ApiException.Unauthorized -> messages.unauthorized
    else -> messages.generic
}
