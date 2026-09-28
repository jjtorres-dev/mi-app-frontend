package com.dvt.medicimapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dvt.medicimapp.data.model.EvaluacionMoskiCheckRequest
import com.dvt.medicimapp.data.repository.MoskiCheckRepository
import com.dvt.medicimapp.presentation.state.MoskiCheckUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoskiCheckViewModel(
    private val repository: MoskiCheckRepository,
    private val savedState: SavedStateHandle,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) : ViewModel() {
    private val restoredAnswers = savedState.get<IntArray>("respuestas")
    private val restored = repository.preguntas.mapIndexedNotNull { index, pregunta ->
        restoredAnswers?.getOrNull(index)?.takeIf { it in 0..1 }?.let { pregunta.campo to it }
    }.toMap()
    private val _uiState = MutableStateFlow(MoskiCheckUiState(
        preguntas = repository.preguntas,
        respuestas = restored,
        iniciado = savedState["iniciado"] ?: false,
        indicePregunta = (savedState.get<Int>("indicePregunta") ?: 0)
            .coerceIn(0, repository.preguntas.lastIndex),
        error = if (restored.size == repository.preguntas.size)
            "La evaluación debe procesarse nuevamente." else null
    ))
    val uiState = _uiState.asStateFlow()
    private var sendJob: Job? = null
    private var session = 0L

    private fun guardar() {
        val state = _uiState.value
        savedState["respuestas"] = state.preguntas.map { state.respuestas[it.campo] ?: -1 }.toIntArray()
        savedState["iniciado"] = state.iniciado
        savedState["indicePregunta"] = state.indicePregunta
    }

    fun iniciar() {
        _uiState.value = _uiState.value.copy(iniciado = true, indicePregunta = 0, error = null)
        guardar()
    }

    fun reiniciarEvaluacion() {
        session++
        sendJob?.cancel()
        sendJob = null
        _uiState.value = MoskiCheckUiState(preguntas = repository.preguntas)
        guardar()
    }

    fun responder(valor: Int) {
        val state = _uiState.value
        if (state.cargando || !state.iniciado || state.resultado != null || valor !in 0..1) return
        val pregunta = state.preguntas[state.indicePregunta]
        val respuestas = state.respuestas + (pregunta.campo to valor)
        _uiState.value = state.copy(
            respuestas = respuestas,
            indicePregunta = (state.indicePregunta + 1).coerceAtMost(state.preguntas.lastIndex)
        )
        guardar()
        if (state.indicePregunta == state.preguntas.lastIndex) enviarEvaluacion()
    }

    fun enviarEvaluacion() {
        if (_uiState.value.cargando || sendJob?.isActive == true) return
        val request = crearSolicitud(_uiState.value.respuestas.toMap())
        val currentSession = session
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        sendJob = viewModelScope.launch(dispatcher) {
            val result = repository.analizarSintomas(request)
            if (currentSession == session) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    resultado = result.getOrNull(),
                    error = if (result.isFailure) result.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                        ?: "No fue posible procesar la evaluación." else null
                )
            }
        }
    }

    private fun crearSolicitud(
        datos: Map<String, Int>
    ): EvaluacionMoskiCheckRequest {

        fun valor(campo: String): Int {
            return datos[campo] ?: 0
        }

        return EvaluacionMoskiCheckRequest(

            suddenFever = valor("sudden_fever"),
            headache = valor("headache"),
            musclePain = valor("muscle_pain"),
            jointPain = valor("joint_pain"),
            vomiting = valor("vomiting"),
            rash = valor("rash"),
            nausea = valor("nausea"),
            fatigue = valor("fatigue"),
            orbitalPain = valor("orbital_pain"),
            redEyes = valor("red_eyes"),
            swelling = valor("swelling"),

            dolorAbdominalIntenso =
                valor("dolor_abdominal_intenso"),

            sangrado =
                valor("sangrado"),

            vomitosPersistentes =
                valor("vomitos_persistentes")
        )
    }

}
