package com.sismantec.acqua.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("Config")
data class ConfigEntity(
    @PrimaryKey val Id: Int,
    val dtePais: String,
    val dteDepto: String,
    val dteMunicipio: String,
    val dteDistrito: String,
    val dteNit: String,
    val dteNrc: String,
    val dteEmisor: String,
    val dteGiro: String,
    val dteNombreComercial: String,
    val dteDireccion: String,
    val dteTelefono: String,
    val dteCorreo: String
)