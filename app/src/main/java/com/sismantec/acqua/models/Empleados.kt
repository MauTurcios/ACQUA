package com.sismantec.acqua.models

data class Empleados(
    var idEmpleado:String,
    var nombreEmpleado:String,
){
    override fun toString(): String {
        return idEmpleado; nombreEmpleado
    }
}