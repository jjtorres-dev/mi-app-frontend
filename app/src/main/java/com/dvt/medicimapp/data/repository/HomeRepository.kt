package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.model.*

interface HomeRepository {
    suspend fun obtenerTotales(): Result<TotalesApi>
    suspend fun obtenerAlertaHoy(distrito: String): Result<AlertaApi>
}
