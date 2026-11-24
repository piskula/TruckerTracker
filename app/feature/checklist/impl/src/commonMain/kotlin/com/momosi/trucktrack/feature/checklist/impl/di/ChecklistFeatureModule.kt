package com.momosi.trucktrack.feature.checklist.impl.di

import com.momosi.trucktrack.feature.checklist.impl.ChecklistViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val checklistFeatureModule = module {
    viewModel { ChecklistViewModel(get(), get()) }
}
