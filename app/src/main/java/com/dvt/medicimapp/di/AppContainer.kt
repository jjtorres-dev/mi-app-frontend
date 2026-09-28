package com.dvt.medicimapp.di

import com.dvt.medicimapp.data.repository.*

class AppContainer {
    val reportsRepository: ReportsRepository by lazy { ApiReportsRepository() }
    val homeRepository: HomeRepository by lazy { ApiHomeRepository() }
    val reportRepository: ReportRepository by lazy { ApiReportRepository() }
    val moskiCheckRepository: MoskiCheckRepository by lazy { ApiMoskiCheckRepository() }

    val guideRepository: GuideRepository by lazy { LocalGuideRepository() }

    val healthcentersRepository: HealthCentersRepository by lazy { LocalHealthCentersRepository() }

    companion object {
        val instance: AppContainer by lazy { AppContainer() }
    }
}
