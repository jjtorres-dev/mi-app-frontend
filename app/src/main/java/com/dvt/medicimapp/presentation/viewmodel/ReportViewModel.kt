package com.dvt.medicimapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dvt.medicimapp.data.repository.ReportRepository
import com.dvt.medicimapp.presentation.state.ReportUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel(
    private val repository: ReportRepository,
    private val savedState: SavedStateHandle,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportUiState(
        nombre = savedState["nombre"] ?: "",
        distrito = savedState["distrito"] ?: "",
        barrio = savedState["barrio"] ?: "",
        descripcion = savedState["descripcion"] ?: "",
        tipoCriadero = savedState["tipoCriadero"] ?: "",
        fotoUri = savedState["fotoUri"],
        esAnonimo = savedState["esAnonimo"] ?: false
    ))
    val uiState = _uiState.asStateFlow()
    private var sendJob: Job? = null

    fun actualizarNombre(value: String) {
        savedState["nombre"] = value
        _uiState.value = _uiState.value.copy(nombre = value)
    }
    fun actualizarDistrito(value: String) {
        savedState["distrito"] = value
        _uiState.value = _uiState.value.copy(distrito = value)
    }
    fun actualizarBarrio(value: String) {
        savedState["barrio"] = value
        _uiState.value = _uiState.value.copy(barrio = value)
    }
    fun actualizarDescripcion(value: String) {
        savedState["descripcion"] = value
        _uiState.value = _uiState.value.copy(descripcion = value)
    }
    fun actualizarTipoCriadero(value: String) {
        savedState["tipoCriadero"] = value
        _uiState.value = _uiState.value.copy(tipoCriadero = value)
    }
    fun actualizarFoto(value: String?) {
        savedState["fotoUri"] = value
        _uiState.value = _uiState.value.copy(fotoUri = value)
    }
    fun actualizarAnonimato(value: Boolean) {
        savedState["esAnonimo"] = value
        _uiState.value = _uiState.value.copy(esAnonimo = value)
    }
    fun cerrarError() { _uiState.value = _uiState.value.copy(error = null) }
    fun confirmarExito() { _uiState.value = _uiState.value.copy(mostrarExito = false) }

    fun enviar() {
        val form = _uiState.value
        if (!form.formularioValido || form.enviando || sendJob?.isActive == true) return
        _uiState.value = form.copy(enviando = true, error = null)
        sendJob = viewModelScope.launch(dispatcher) {
            val result = repository.enviarReporte(
                nombre = if (form.esAnonimo) null else form.nombre,
                distrito = form.distrito,
                barrio = form.barrio.ifBlank { null },
                tipoCriadero = form.tipoCriadero,
                descripcion = form.descripcion.ifBlank { null },
                esAnonimo = form.esAnonimo
            )
            _uiState.value = _uiState.value.copy(
                enviando = false,
                mostrarExito = result.isSuccess,
                error = if (result.isFailure) result.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                    ?: "No se pudo conectar con el servidor. Verifica tu conexión." else null
            )
        }
    }
}
