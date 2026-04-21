package com.sismantec.acqua.models

import com.sismantec.acqua.entities.ClientesEntity

data class DatosAvisoCobro(
    val cliente: ClientesEntity,
    val lecturaActual: Int,
    val lecturaAnterior: Int,
    val consumo: Int,
    val total: Int
)