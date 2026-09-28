package com.dvt.medicimapp.data.model

data class AlertaApi(
    val distrito: String,
    val nivel: String,
    val totalReportes: Int,
    val descripcion: String
)
