package com.momosi.trucktrack.core.issue.api

import com.momosi.trucktrack.shared.common.PageDto
import com.momosi.trucktrack.shared.issue.IssueAttachmentDto
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.ReqBuilder
import io.ktor.client.request.HttpRequestBuilder

interface IssueAttachmentApi {

    @GET("api/v1/issue/{issueId}/photo")
    suspend fun getPhotoList(
        @Path("issueId") issueId: Long,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null,
    ): PageDto<IssueAttachmentDto>

    @POST("api/v1/issue/{issueId}/photo")
    suspend fun uploadPhoto(@Path("issueId") issueId: Long, @ReqBuilder builder: HttpRequestBuilder.() -> Unit): IssueAttachmentDto

    @Headers("Accept: application/octet-stream")
    @GET("api/v1/issue/{issueId}/photo/{attachmentId}")
    suspend fun downloadPhoto(@Path("issueId") issueId: Long, @Path("attachmentId") attachmentId: Long): ByteArray

    @DELETE("api/v1/issue/{issueId}/photo/{attachmentId}")
    suspend fun deletePhoto(@Path("issueId") issueId: Long, @Path("attachmentId") attachmentId: Long)
}
