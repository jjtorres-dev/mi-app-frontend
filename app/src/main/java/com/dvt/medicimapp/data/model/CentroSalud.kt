package com.dvt.medicimapp.data.model

data class CentroSalud(
    val nombre: String,
    val tipo: String,
    val direccion: String,
    val telefono: String,
    val lat: Double,
    val lon: Double
)
