package com.dvt.medicimapp.presentation.state

import com.dvt.medicimapp.data.model.ReporteApi
import com.dvt.medicimapp.data.model.ReporteZonaApi

data class ReportsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val zonas: List<ReporteZonaApi> = emptyList(),
    val reportesRecientes: List<ReporteApi> = emptyList(),
    val totalReportes: Int? = null,
    val totalDistritos: Int? = null,
    val errorEstadisticas: String? = null
)
