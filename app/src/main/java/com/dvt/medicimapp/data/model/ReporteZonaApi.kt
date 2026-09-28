package com.dvt.medicimapp.data.model

data class ReporteZonaApi(
    val distrito: String,
    val totalReportes: Int,
    val recipiente: Int,
    val llanta: Int,
    val acequia: Int,
    val maleza: Int,
    val basura: Int
)
