package com.dvt.medicimapp.data.model

data class ReporteApi(
    val id: Int,
    val nombre: String,
    val distrito: String,
    val barrio: String?,
    val tipoCriadero: String,
    val descripcion: String?,
    val fechaHora: String,
    val esAnonimo: Boolean,
    val estado: String
)
