package com.momosi.trucktrack.core.issue

import com.momosi.trucktrack.core.common.TruckTrackConfig
import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.model.Page
import com.momosi.trucktrack.core.common.network.onNetworkFailure
import com.momosi.trucktrack.core.common.network.onNoConnectionFailure
import com.momosi.trucktrack.core.issue.api.IssueAttachmentApi
import com.momosi.trucktrack.core.issue.dto.toIssueAttachment
import com.momosi.trucktrack.core.issue.model.IssueAttachment
import com.momosi.trucktrack.core.network.dto.toPage
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class IssueAttachmentRepositoryImpl(private val issueAttachmentApi: IssueAttachmentApi) : IssueAttachmentRepository {

    override fun getPhotoUrl(issueId: Long, attachmentId: Long): String = "${TruckTrackConfig.API_BASE_URL}api/v1/issue/$issueId/photo/$attachmentId"

    override suspend fun getPhotos(
        issueId: Long,
        page: Int?,
        size: Int?,
        sort: String?,
    ): Result<Page<IssueAttachment>> = runCatchingCancellable {
        issueAttachmentApi.getPhotoList(
            issueId = issueId,
            page = page,
            size = size,
            sort = sort,
        ).toPage { it.toIssueAttachment() }
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to get photos for issue $issueId (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to get photos for issue $issueId") }

    override suspend fun uploadPhoto(
        issueId: Long,
        fileName: String,
        fileBytes: ByteArray,
        contentType: String,
    ): Result<IssueAttachment> = runCatchingCancellable {
        issueAttachmentApi.uploadPhoto(issueId) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "file",
                            fileBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            },
                        )
                    },
                ),
            )
        }.toIssueAttachment()
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to upload photo for issue $issueId (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to upload photo for issue $issueId") }

    override suspend fun downloadPhoto(issueId: Long, attachmentId: Long): Result<ByteArray> = runCatchingCancellable {
        issueAttachmentApi.downloadPhoto(issueId, attachmentId)
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to download photo $attachmentId for issue $issueId (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to download photo $attachmentId for issue $issueId") }

    override suspend fun deletePhoto(issueId: Long, attachmentId: Long): Result<Unit> = runCatchingCancellable {
        issueAttachmentApi.deletePhoto(issueId, attachmentId)
    }
        .onNoConnectionFailure { Logger.w(TAG, it, "Failed to delete photo $attachmentId for issue $issueId (offline)") }
        .onNetworkFailure { Logger.e(TAG, it, "Failed to delete photo $attachmentId for issue $issueId") }
}
