package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.model.CentroSalud

class LocalHealthCentersRepository : HealthCentersRepository {
    private val centros = listOf(
        CentroSalud("Hospital II-2 Tarapoto", "Hospital", "Jr. Salaverry N° 1215, Tarapoto", "042522331", -6.4851, -76.3615),
        CentroSalud("C.S. Morales", "Centro de Salud", "Jr. Los Pinos S/N, Morales", "042521890", -6.4920, -76.3700),
        CentroSalud("C.S. Banda de Shilcayo", "Centro de Salud", "Av. Circunvalación S/N, Banda de Shilcayo", "042523145", -6.4700, -76.3480),
        CentroSalud("Posta Médica Partido Alto", "Posta Médica", "Jr. Amazonas S/N, Partido Alto", "042524010", -6.4780, -76.3550),
        CentroSalud("C.S. Juan Guerra", "Centro de Salud", "Plaza Principal S/N, Juan Guerra", "042525200", -6.5010, -76.3800),
        CentroSalud("Clínica San Martín", "Clínica Privada", "Jr. Martínez de Compagnon 812, Tarapoto", "042526000", -6.4860, -76.3630),
    )

    override fun obtenerCentros(): List<CentroSalud> = centros
}
