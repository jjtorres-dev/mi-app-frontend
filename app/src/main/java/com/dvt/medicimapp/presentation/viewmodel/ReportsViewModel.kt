package com.dvt.medicimapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dvt.medicimapp.data.repository.ReportsRepository
import com.dvt.medicimapp.presentation.state.ReportsUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val repository: ReportsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init { cargar() }

    fun cargar() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch(dispatcher) {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val zonas = repository.obtenerReportesPorDistrito()
            val reportes = repository.obtenerReportes()
            val totales = repository.obtenerTotales()
            _uiState.value = ReportsUiState(
                loading = false,
                error = when {
                    zonas.isFailure -> "No se pudo cargar el ranking de zonas"
                    reportes.isFailure -> "No se pudo cargar los reportes recientes"
                    else -> null
                },
                zonas = zonas.getOrDefault(emptyList()),
                reportesRecientes = reportes.getOrDefault(emptyList()).take(10),
                totalReportes = totales.getOrNull()?.totalReportes,
                totalDistritos = totales.getOrNull()?.totalDistritos,
                errorEstadisticas = totales.exceptionOrNull()?.message
            )
        }
    }
}
