package com.dvt.medicimapp.presentation.state

import com.dvt.medicimapp.data.model.AlertaApi
import com.dvt.medicimapp.data.model.TotalesApi

enum class LoadStatus { Loading, Success, Error }

data class HomeUiState(
    val totales: TotalesApi? = null,
    val alerta: AlertaApi? = null,
    val totalesStatus: LoadStatus = LoadStatus.Loading,
    val alertaStatus: LoadStatus = LoadStatus.Loading,
    val errorTotales: String? = null,
    val errorAlerta: String? = null
)
