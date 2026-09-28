package com.dvt.medicimapp.presentation.state

data class ReportUiState(
    val nombre: String = "",
    val distrito: String = "",
    val barrio: String = "",
    val descripcion: String = "",
    val tipoCriadero: String = "",
    val fotoUri: String? = null,
    val esAnonimo: Boolean = false,
    val enviando: Boolean = false,
    val mostrarExito: Boolean = false,
    val error: String? = null
) {
    val formularioValido: Boolean
        get() = distrito.isNotBlank() && tipoCriadero.isNotBlank() &&
            (esAnonimo || nombre.isNotBlank())
}
