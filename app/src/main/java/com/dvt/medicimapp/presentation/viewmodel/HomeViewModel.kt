package com.dvt.medicimapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dvt.medicimapp.data.repository.HomeRepository
import com.dvt.medicimapp.presentation.state.HomeUiState
import com.dvt.medicimapp.presentation.state.LoadStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init { cargar() }

    fun cargar() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch(dispatcher) {
            _uiState.value = HomeUiState()
            val totales = repository.obtenerTotales()
            _uiState.value = _uiState.value.copy(
                totales = totales.getOrNull(),
                totalesStatus = if (totales.isSuccess) LoadStatus.Success else LoadStatus.Error,
                errorTotales = totales.exceptionOrNull()?.message
            )
            val alerta = repository.obtenerAlertaHoy("Tarapoto")
            _uiState.value = _uiState.value.copy(
                alerta = alerta.getOrNull(),
                alertaStatus = if (alerta.isSuccess) LoadStatus.Success else LoadStatus.Error,
                errorAlerta = alerta.exceptionOrNull()?.message
            )
        }
    }
}
