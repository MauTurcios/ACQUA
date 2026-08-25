package com.sismantec.acqua.models

import java.math.BigDecimal

data class LecturaRequest(
    val cuenta: String,
    val idLectura: Int,
    val lectura_actual: Double,
    val empleado: String
)