package com.dvt.medicimapp.presentation.state

import com.dvt.medicimapp.data.model.CentroSalud

data class HealthCentersUiState(
    val centros: List<CentroSalud>,
    val tabSeleccionado: Int = 0
)
