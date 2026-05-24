package com.sismantec.acqua.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sismantec.acqua.models.ConsumoResponse

@Entity ("lectura")
data class LecturaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val Id_cliente: Int,
    val Lectura_anterior: String,
    val Lectura_actual: String,
    val Consumo: String
)