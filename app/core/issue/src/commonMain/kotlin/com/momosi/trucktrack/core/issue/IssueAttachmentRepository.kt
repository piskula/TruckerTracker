package com.momosi.trucktrack.core.issue

import com.momosi.trucktrack.core.common.model.Page
import com.momosi.trucktrack.core.issue.model.IssueAttachment

interface IssueAttachmentRepository {

    fun getPhotoUrl(issueId: Long, attachmentId: Long): String

    suspend fun getPhotos(
        issueId: Long,
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): Result<Page<IssueAttachment>>

    suspend fun uploadPhoto(
        issueId: Long,
        fileName: String,
        fileBytes: ByteArray,
        contentType: String,
    ): Result<IssueAttachment>

    suspend fun downloadPhoto(issueId: Long, attachmentId: Long): Result<ByteArray>

    suspend fun deletePhoto(issueId: Long, attachmentId: Long): Result<Unit>
}
