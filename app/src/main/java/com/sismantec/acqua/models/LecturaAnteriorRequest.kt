package com.sismantec.acqua.models

data class LecturaAnteriorRequest(
    val cuenta: String,
    val idLectura: Int,
    val lectura_anterior: Double,
)
