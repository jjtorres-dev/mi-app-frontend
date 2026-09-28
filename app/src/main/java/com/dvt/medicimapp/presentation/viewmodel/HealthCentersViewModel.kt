package com.dvt.medicimapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dvt.medicimapp.data.repository.HealthCentersRepository
import com.dvt.medicimapp.presentation.state.HealthCentersUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HealthCentersViewModel(
    repository: HealthCentersRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(HealthCentersUiState(
        centros = repository.obtenerCentros(),
        tabSeleccionado = (savedState.get<Int>("tab") ?: 0).coerceIn(0, 1)
    ))
    val uiState = _uiState.asStateFlow()

    fun seleccionarTab(index: Int) {
        if (index !in 0..1) return
        savedState["tab"] = index
        _uiState.value = _uiState.value.copy(tabSeleccionado = index)
    }
}
