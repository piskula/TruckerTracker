package com.momosi.trucktrack.core.issue.di

import com.momosi.trucktrack.core.issue.IssueAttachmentRepository
import com.momosi.trucktrack.core.issue.IssueAttachmentRepositoryImpl
import com.momosi.trucktrack.core.issue.IssueCapabilityRepository
import com.momosi.trucktrack.core.issue.IssueCapabilityRepositoryImpl
import com.momosi.trucktrack.core.issue.IssueRepository
import com.momosi.trucktrack.core.issue.IssueRepositoryImpl
import com.momosi.trucktrack.core.issue.api.createIssueApi
import com.momosi.trucktrack.core.issue.api.createIssueAttachmentApi
import com.momosi.trucktrack.core.issue.api.createIssueHistoryApi
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.dsl.module

val issueModule = module {
    single { get<Ktorfit>().createIssueApi() }
    single { get<Ktorfit>().createIssueHistoryApi() }
    single { get<Ktorfit>().createIssueAttachmentApi() }
    single<IssueRepository> { IssueRepositoryImpl(get(), get()) }
    single<IssueAttachmentRepository> { IssueAttachmentRepositoryImpl(get()) }
    single<IssueCapabilityRepository> { IssueCapabilityRepositoryImpl() }
}
