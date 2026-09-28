package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.model.*

interface MoskiCheckRepository {
    val preguntas: List<com.dvt.medicimapp.data.model.PreguntaMoskiCheck>
    suspend fun analizarSintomas(evaluacion: EvaluacionMoskiCheckRequest): Result<ResultadoMoskiCheckApi>
}
