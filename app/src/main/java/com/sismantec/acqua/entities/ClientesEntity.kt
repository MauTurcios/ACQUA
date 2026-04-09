package com.sismantec.acqua.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("Clientes")
data class ClientesEntity(
    @PrimaryKey val Id: Int,
    val Codigo: String,
    val Cliente: String,
    val Casa: String?,
    val Poligono: String?,
    val Id_ruta: Int,
    val Codigo_casa: String?
)