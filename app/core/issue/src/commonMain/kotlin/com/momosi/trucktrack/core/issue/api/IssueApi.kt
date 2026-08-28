package com.momosi.trucktrack.core.issue.api

import com.momosi.trucktrack.shared.common.PageDto
import com.momosi.trucktrack.shared.issue.IssueCreateDto
import com.momosi.trucktrack.shared.issue.IssueDto
import com.momosi.trucktrack.shared.issue.IssueFilterDto
import com.momosi.trucktrack.shared.issue.IssueHistoryDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface IssueApi {

    @POST("api/v1/issue")
    suspend fun getIssueList(
        @Body filter: IssueFilterDto,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null,
    ): PageDto<IssueDto>

    @POST("api/v1/issue/create")
    suspend fun createIssue(@Body body: IssueCreateDto): IssueDto

    @GET("api/v1/issue/byId/{id}")
    suspend fun getIssue(@Path("id") id: Long): IssueDto

    @POST("api/v1/issue/{id}/start")
    suspend fun startIssue(@Path("id") id: Long): IssueDto

    @POST("api/v1/issue/{id}/resolve")
    suspend fun resolveIssue(@Path("id") id: Long): IssueDto

    @POST("api/v1/issue/{id}/cancel")
    suspend fun cancelIssue(@Path("id") id: Long): IssueDto

    @POST("api/v1/issue/{id}/assignTome")
    suspend fun assignIssue(@Path("id") id: Long): IssueDto

    @POST("api/v1/issue/{id}/comment")
    suspend fun addComment(@Path("id") id: Long, @Body comment: String): IssueHistoryDto
}
