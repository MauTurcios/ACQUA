package com.sismantec.acqua.models

import com.google.gson.annotations.SerializedName

data class RespuestaConexion (
    @SerializedName("respuesta") val respuesta : String
)