package com.momosi.trucktrack.feature.issues.impl.di

import com.momosi.trucktrack.feature.issues.impl.create.CreateIssueViewModel
import com.momosi.trucktrack.feature.issues.impl.detail.IssueDetailViewModel
import com.momosi.trucktrack.feature.issues.impl.list.IssuesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val issuesModule = module {
    viewModel { IssuesViewModel(get(), get()) }
    viewModel { params -> IssueDetailViewModel(params.get(), get(), get(), get(), get(), get()) }
    viewModel { CreateIssueViewModel(get(), get(), get(), get()) }
}
