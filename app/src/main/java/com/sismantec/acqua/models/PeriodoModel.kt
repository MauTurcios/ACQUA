package com.sismantec.acqua.models

data class PeriodoModel (
    val id: Int,
    val idCatalogo: Int,
    val fecha: String,
    val periodo_inicio: String,
    val periodo_fin: String,
    val fecha_vencimiento: String,
    val concepto: String,
    val estado: String
)