package com.dvt.medicimapp.data.model

data class EvaluacionMoskiCheckRequest(

    // 11 síntomas del modelo
    val suddenFever: Int,
    val headache: Int,
    val musclePain: Int,
    val jointPain: Int,
    val vomiting: Int,
    val rash: Int,
    val nausea: Int,
    val fatigue: Int,
    val orbitalPain: Int,
    val redEyes: Int,
    val swelling: Int,

    // 3 señales de seguridad
    val dolorAbdominalIntenso: Int,
    val sangrado: Int,
    val vomitosPersistentes: Int
)
