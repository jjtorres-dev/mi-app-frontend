package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.ApiService
import com.dvt.medicimapp.data.model.*

class ApiReportsRepository : ReportsRepository {
    override suspend fun obtenerReportes(): Result<List<ReporteApi>> =
        repositoryResult { ApiService.obtenerReportes() }

    override suspend fun obtenerReportesPorDistrito(): Result<List<ReporteZonaApi>> =
        repositoryResult { ApiService.obtenerReportesPorDistrito() }

    override suspend fun obtenerTotales(): Result<TotalesApi> =
        repositoryResult { ApiService.obtenerTotales() }
}
