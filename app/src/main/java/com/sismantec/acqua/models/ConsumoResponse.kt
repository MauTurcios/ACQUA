package com.sismantec.acqua.models

data class ConsumoResponse(
    val consumo : String,
    val lecturaAnterior: String,
    val lecturaActual: String
)