package com.sismantec.acqua.models

data class LecturaConAnteriorRequest(
    val cuenta: String,
    val idLectura: Int,
    val lectura_anterior: Double,
    val lectura_actual: Double,
    val empleado: String
)
