package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.model.*

interface ReportRepository {
    suspend fun enviarReporte(nombre: String?, distrito: String, barrio: String?, tipoCriadero: String, descripcion: String?, esAnonimo: Boolean): Result<Int>
}
