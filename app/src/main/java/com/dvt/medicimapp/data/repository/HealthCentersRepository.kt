package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.model.CentroSalud

interface HealthCentersRepository {
    fun obtenerCentros(): List<CentroSalud>
}
