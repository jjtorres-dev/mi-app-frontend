package com.dvt.medicimapp.data.model

data class ResultadoMoskiCheckApi(
    val status: String,
    val prediction: String?,
    val confidence: Double?,
    val probabilities: ProbabilidadesMoskiCheckApi?,
    val symptomCount: Int,
    val alertsDetected: List<String>,
    val modelVersion: String?,
    val message: String
)
