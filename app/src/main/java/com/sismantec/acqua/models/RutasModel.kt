package com.sismantec.acqua.models
import com.google.gson.annotations.SerializedName

data class RutasModel(
    @SerializedName("id") val Id: Int,
    @SerializedName("ruta") val Ruta: String,
)