package com.sismantec.acqua.models

import java.sql.Date


data class ConsumoResponse(
    val cuenta: String,
    val nombre: String,
    val periodo: Int,
    val lecturaAnterior: Double,
    val lecturaActual: Double,
    val consumo: Int,
    val appPrefacturado: Boolean,
    val usuario: String,
    val documento: String,
    val direccion: String,
    val idColbar: Int,
    val colbar: String,
    val idSector: Int,
    val sector: String,
    val idZona: Int,
    val zona: String,
    val cf1_linea: String,
    val cf1_descripcion: String,
    val cf1_precio: Double,
    val cf2_linea: String,
    val cf2_descripcion: String,
    val cf2_precio: Double,
    val cf3_linea: String,
    val cf3_descripcion: String,
    val cf3_precio: Double,
    val cf4_linea: String,
    val cf4_descripcion: String,
    val cf4_precio: Double,
    val cf5_linea: String,
    val cf5_descripcion: String,
    val cf5_precio: Double
)