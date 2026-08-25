package com.sismantec.acqua.controller

import com.sismantec.acqua.Util.PeriodoPreferences
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.sismantec.acqua.apiservices.RetrofitCliente
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.models.PeriodoModel

class PeriodoController {

    private  var funciones = Funciones()
    //private lateinit var preferences: SharedPreferences
    //private var instancia = "CONFIG_SERVIDOR"
    //private var instancia_periodo = "PERIODO_PREFACTURA"



    private suspend fun obtenerPeriodoServidor(idPeriodo: Int, context: Context): PeriodoModel?{
        val servidor = funciones.obtenerServidor(context)
        val api = RetrofitCliente.obtenerApi(servidor)
        return try {
            val response = api.obtenerPeriodo(idPeriodo)
            when{
                response.code() == 404 ->{
                    Toast.makeText(context, "El periodo no existe", Toast.LENGTH_SHORT).show()
                    null
                }
                !response.isSuccessful -> {
                    Toast.makeText(context, "Error al consultar el periodo, intente nuevamente", Toast.LENGTH_SHORT).show()
                    null
                }else -> response.body()
            }
        }catch (e: Exception){
            Toast.makeText(context, "Error: "+e.message, Toast.LENGTH_SHORT).show()
            null
        }
    }

    suspend fun obtenerPeriodo(idPeriodo: Int, context: Context): Boolean {
        val periodo = obtenerPeriodoServidor(idPeriodo, context)?: return false

        return procesarPeriodo(context, periodo)
    }

    suspend fun verificarPeriodo(idPeriodo: Int, context: Context): Boolean {
        val periodo = obtenerPeriodoServidor(idPeriodo, context)?: return false

        return periodo.estado.equals("ABIERTA", ignoreCase = true)
    }

    private fun procesarPeriodo(context: Context, periodo: PeriodoModel): Boolean{
        if (!periodo.estado.equals("ABIERTA", ignoreCase = true)){
            Toast.makeText(context, "El periodo ya está cerrado", Toast.LENGTH_SHORT).show()
            return false
        }
        val preferences = PeriodoPreferences(context)
        preferences.guardarPeriodo(
            periodo.id,
            periodo.idCatalogo,
            periodo.periodo_inicio,
            periodo.periodo_fin,
            periodo.fecha_vencimiento,
            periodo.concepto,
            periodo.estado
        )
        return true
    }

}