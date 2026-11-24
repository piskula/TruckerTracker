package com.momosi.trucktrack.core.push.registration

import android.content.Context
import android.os.Build
import com.jaredrummler.android.device.DeviceName
import com.momosi.trucktrack.shared.push.PushPlatformDto
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

private const val FALLBACK_DEVICE_NAME = "Android"
private val DEVICE_NAME_LOOKUP_TIMEOUT = 5.seconds

internal class AndroidPushDeviceInfo(private val context: Context) : PushDeviceInfo {

    override val platform: PushPlatformDto = PushPlatformDto.ANDROID

    @Volatile
    private var resolvedDeviceName: String? = null

    override suspend fun deviceName(): String = resolvedDeviceName ?: resolveDeviceName().also { resolvedDeviceName = it }

    private suspend fun resolveDeviceName(): String = withTimeoutOrNull(DEVICE_NAME_LOOKUP_TIMEOUT) { lookUpMarketingName() }
        ?: manufacturerAndModelName()

    private suspend fun lookUpMarketingName(): String? = suspendCancellableCoroutine { continuation ->
        DeviceName.with(context).request { info, error ->
            continuation.resume(if (error == null) info?.marketingName() else null)
        }
    }
}

private fun DeviceName.DeviceInfo.marketingName(): String? {
    val marketName = marketName?.trim().orEmpty()
    if (marketName.isEmpty() || marketName.equals(model?.trim(), ignoreCase = true)) return null
    return withManufacturer(Build.MANUFACTURER, marketName)
}

private fun manufacturerAndModelName(): String = withManufacturer(Build.MANUFACTURER, Build.MODEL.orEmpty().trim())
    .ifBlank { FALLBACK_DEVICE_NAME }

private fun withManufacturer(manufacturer: String?, name: String): String {
    val capitalizedManufacturer = manufacturer.orEmpty().trim().replaceFirstChar { it.titlecase() }
    val fullName = when {
        capitalizedManufacturer.isEmpty() -> name
        name.startsWith(capitalizedManufacturer, ignoreCase = true) -> name
        else -> "$capitalizedManufacturer $name"
    }
    return fullName.replaceFirstChar { it.titlecase() }
}
