package com.dvt.medicimapp.presentation.state

import com.dvt.medicimapp.data.model.ResultadoMoskiCheckApi
import com.dvt.medicimapp.data.model.PreguntaMoskiCheck

data class MoskiCheckUiState(
    val preguntas: List<PreguntaMoskiCheck>,
    val respuestas: Map<String, Int> = emptyMap(),
    val iniciado: Boolean = false,
    val indicePregunta: Int = 0,
    val cargando: Boolean = false,
    val resultado: ResultadoMoskiCheckApi? = null,
    val error: String? = null
)
