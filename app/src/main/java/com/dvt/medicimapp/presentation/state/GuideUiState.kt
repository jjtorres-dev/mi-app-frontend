package com.dvt.medicimapp.presentation.state

import com.dvt.medicimapp.data.model.GuiaItem

data class GuideUiState(
    val categorias: List<String>,
    val categoriaSeleccionada: String,
    val guias: List<GuiaItem>
)
