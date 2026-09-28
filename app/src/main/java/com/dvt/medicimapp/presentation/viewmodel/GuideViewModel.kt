package com.dvt.medicimapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dvt.medicimapp.data.repository.GuideRepository
import com.dvt.medicimapp.presentation.state.GuideUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GuideViewModel(
    private val repository: GuideRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val guias = repository.obtenerGuias()
    private val categoria = savedState.get<String>("categoria")
        ?.takeIf { it in repository.categorias } ?: "Todas"
    private val _uiState = MutableStateFlow(estado(categoria))
    val uiState = _uiState.asStateFlow()

    private fun estado(categoria: String) = GuideUiState(
        categorias = repository.categorias,
        categoriaSeleccionada = categoria,
        guias = if (categoria == "Todas") guias else guias.filter { it.categoria == categoria }
    )

    fun seleccionarCategoria(categoria: String) {
        if (categoria !in repository.categorias) return
        savedState["categoria"] = categoria
        _uiState.value = estado(categoria)
    }
}
