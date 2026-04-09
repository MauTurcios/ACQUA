package com.sismantec.acqua.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("Rutas")
data class RutasEntity(
    @PrimaryKey val Id: Int,
    val Ruta : String
)
