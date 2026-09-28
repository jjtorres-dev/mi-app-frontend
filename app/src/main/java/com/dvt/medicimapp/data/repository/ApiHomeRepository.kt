package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.ApiService
import com.dvt.medicimapp.data.model.*

class ApiHomeRepository : HomeRepository {
    override suspend fun obtenerTotales(): Result<TotalesApi> =
        repositoryResult { ApiService.obtenerTotales() }

    override suspend fun obtenerAlertaHoy(distrito: String): Result<AlertaApi> =
        repositoryResult { ApiService.obtenerAlertaHoy(distrito) }
}
