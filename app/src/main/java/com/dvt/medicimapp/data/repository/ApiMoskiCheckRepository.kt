package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.ApiService
import com.dvt.medicimapp.data.model.*

class ApiMoskiCheckRepository : MoskiCheckRepository {
    override val preguntas get() = preguntasMoskiCheck
    override suspend fun analizarSintomas(evaluacion: EvaluacionMoskiCheckRequest): Result<ResultadoMoskiCheckApi> =
        repositoryResult { ApiService.analizarSintomas(evaluacion) }
}
