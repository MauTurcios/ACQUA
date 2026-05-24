package com.sismantec.acqua.models

import com.sismantec.acqua.entities.ClientesEntity

data class DatosAvisoCobro(
    val cliente: ClientesEntity,
    val lecturaActual: String,
    val lecturaAnterior: String,
    val consumo: String,
    val total: String
)