package com.sismantec.acqua.models

data class ErrorLecturaRequest (
    val mensaje: String? = null,
    val inner: String? = null,
    val tipo: String? = null
)