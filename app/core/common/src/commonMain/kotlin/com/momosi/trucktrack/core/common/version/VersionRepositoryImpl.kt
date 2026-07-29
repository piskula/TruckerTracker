package com.momosi.trucktrack.core.common.version

import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.onNetworkFailure
import com.momosi.trucktrack.core.common.network.onNoConnectionFailure
import com.momosi.trucktrack.core.common.version.api.VersionApi
import com.momosi.trucktrack.core.common.version.dto.toServerVersion
import com.momosi.trucktrack.core.common.version.model.ServerVersion

private const val TAG = "VersionRepository"

class VersionRepositoryImpl(private val versionApi: VersionApi) : VersionRepository {

    override suspend fun getServerVersion(): Result<ServerVersion> = runCatchingCancellable {
        versionApi.getBuildInfo().toServerVersion()
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to get server version (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to get server version") }
}
