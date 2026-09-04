package com.momosi.trucktrack.feature.issues.impl.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
@Stable
sealed interface PhotoSource {
    val fileName: String

    @Serializable
    data class Attachment(val issueId: Long, val attachmentId: Long, val url: String, override val fileName: String) : PhotoSource

    @Serializable
    class Bytes(val bytes: ByteArray, override val fileName: String) : PhotoSource {
        override fun equals(other: Any?): Boolean = other is Bytes && bytes.contentEquals(other.bytes) && fileName == other.fileName
        override fun hashCode(): Int = 31 * bytes.contentHashCode() + fileName.hashCode()
    }
}

@Serializable
data class FullScreenPhotoNavKey(val source: PhotoSource) : NavKey
