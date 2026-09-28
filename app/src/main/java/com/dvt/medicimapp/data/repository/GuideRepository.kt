package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.model.GuiaItem

interface GuideRepository {
    val categorias: List<String>
    fun obtenerGuias(): List<GuiaItem>
}
