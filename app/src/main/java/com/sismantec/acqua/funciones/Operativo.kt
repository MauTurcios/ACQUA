package com.sismantec.acqua.funciones

import android.content.Context
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.entities.ConfigEntity
import kotlinx.coroutines.flow.first

class Operativo(private val context: Context) {
    private var config: ConfigEntity? = null
    suspend fun cargarConfig(){
        val db = AppDataBase.obtenerInstancia(context)
        config = db.ConfigDAO().obtenerConfig().first()
    }
    fun empresa() = config?.dteEmisor ?: ""
    fun giro() = config?.dteGiro ?: ""
    fun nrc() = config?.dteNrc ?: ""
    fun nit() = config?.dteNit ?: ""
    fun direccion() = config?.dteDireccion?:""
}