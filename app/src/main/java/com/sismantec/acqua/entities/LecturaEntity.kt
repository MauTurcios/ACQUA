package com.sismantec.acqua.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("lectura")
data class LecturaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val Cuenta: String,
    val Nombre: String,
    val Periodo: Int,
    val Lectura_anterior: Double,
    val Lectura_actual: Double,
    val Consumo: Int,
    val Usuario: String,
    val Documento: String,
    val Direccion: String,
    val IdColbar: Int,
    val Colbar: String,
    val IdSector: Int,
    val Sector: String,
    val IdZona: Int,
    val Zona: String,
    val Lectura_enviada: Boolean = false,
    val Cf1_linea: String,
    val Cf1_descripcion: String,
    val Cf1_precio: Double,
    val Cf2_linea: String,
    val Cf2_descripcion: String,
    val Cf2_precio: Double,
    val Cf3_linea: String,
    val Cf3_descripcion: String,
    val Cf3_precio: Double,
    val Cf4_linea: String,
    val Cf4_descripcion: String,
    val Cf4_precio: Double,
    val Cf5_linea: String,
    val Cf5_descripcion: String,
    val Cf5_precio: Double
)