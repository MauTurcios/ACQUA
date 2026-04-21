package com.sismantec.acqua.models
import com.google.gson.annotations.SerializedName

data class Cliente (
    @SerializedName("id") val Id: Int,
    @SerializedName("codigo") val Codigo: String,
    @SerializedName("cliente") val Cliente: String,
    @SerializedName("casa") val Casa: String? ="0",
    @SerializedName("poligono") val Poligono: String?="0",
    @SerializedName("id_ruta") val Id_ruta: Int,
    @SerializedName("codigo_casa") val Codigo_casa: String?="0",
    @SerializedName("direccion") val Direccion: String? = ""
    )
