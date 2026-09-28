package com.dvt.medicimapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import com.dvt.medicimapp.presentation.viewmodel.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

/** Constructor injection, scoped by the NavBackStackEntry that requests the ViewModel. */
class AppViewModelFactory(
    private val container: AppContainer = AppContainer.instance
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass == ReportsViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(container.reportsRepository) as T
        }
        if (modelClass == HomeViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(container.homeRepository) as T
        }
        if (modelClass == ReportViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(container.reportRepository, extras.createSavedStateHandle()) as T
        }
        if (modelClass == MoskiCheckViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return MoskiCheckViewModel(container.moskiCheckRepository, extras.createSavedStateHandle()) as T
        }
        if (modelClass == GuideViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return GuideViewModel(container.guideRepository, extras.createSavedStateHandle()) as T
        }
        if (modelClass == HealthCentersViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return HealthCentersViewModel(container.healthcentersRepository, extras.createSavedStateHandle()) as T
        }
        throw IllegalArgumentException("ViewModel no registrado: ${modelClass.name}")
    }
}
