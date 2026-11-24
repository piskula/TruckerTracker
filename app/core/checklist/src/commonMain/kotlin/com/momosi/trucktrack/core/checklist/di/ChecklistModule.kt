package com.momosi.trucktrack.core.checklist.di

import com.momosi.trucktrack.core.checklist.ChecklistRepository
import com.momosi.trucktrack.core.checklist.ChecklistRepositoryImpl
import com.momosi.trucktrack.core.checklist.api.createChecklistQuestionApi
import com.momosi.trucktrack.core.checklist.api.createChecklistResponseApi
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.dsl.module

val checklistModule = module {
    single { get<Ktorfit>().createChecklistQuestionApi() }
    single { get<Ktorfit>().createChecklistResponseApi() }
    single<ChecklistRepository> { ChecklistRepositoryImpl(get(), get()) }
}
