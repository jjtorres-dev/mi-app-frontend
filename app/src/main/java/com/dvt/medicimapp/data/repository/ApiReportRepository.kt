package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.ApiService
import com.dvt.medicimapp.data.model.*

class ApiReportRepository : ReportRepository {
    override suspend fun enviarReporte(nombre: String?, distrito: String, barrio: String?, tipoCriadero: String, descripcion: String?, esAnonimo: Boolean): Result<Int> =
        repositoryResult { ApiService.enviarReporte(nombre, distrito, barrio, tipoCriadero, descripcion, esAnonimo) }
}
