package com.sismantec.acqua.models

import com.google.gson.annotations.SerializedName

data class RespuestaLogin(
    @SerializedName("idVendedor") val idEmpleado: Int,
    @SerializedName("nombreVendedor") val nombreEmpleado: String,
    @SerializedName("respuesta") val respuestaServidor: String
)