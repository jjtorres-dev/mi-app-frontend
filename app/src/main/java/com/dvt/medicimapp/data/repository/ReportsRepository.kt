package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.model.*

interface ReportsRepository {
    suspend fun obtenerReportes(): Result<List<ReporteApi>>
    suspend fun obtenerReportesPorDistrito(): Result<List<ReporteZonaApi>>
    suspend fun obtenerTotales(): Result<TotalesApi>
}
