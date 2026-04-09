package com.sismantec.acqua.models
import com.google.gson.annotations.SerializedName

data class ConfigModel(
    @SerializedName("id") val Id: Int,
    @SerializedName("dtePais") val dtePais: String,
    @SerializedName("dteDepto") val dteDepto: String,
    @SerializedName("dteMunicipio") val dteMunicipio: String,
    @SerializedName("dteDistrito") val dteDistrito: String,
    @SerializedName("dteNit") val dteNit: String,
    @SerializedName("dteNrc") val dteNrc: String,
    @SerializedName("dteNombreEmisor") val dteNombreEmisor: String,
    @SerializedName("dteGiro") val dteGiro: String,
    @SerializedName("dteNombreComercial") val dteNombreComercial: String,
    @SerializedName("dteDireccion") val dteDireccion: String,
    @SerializedName("dteTelefono") val dteTelefono: String,
    @SerializedName("dteCorreo") val dteCorreo: String
    )