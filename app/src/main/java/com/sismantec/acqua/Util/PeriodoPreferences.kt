package com.sismantec.acqua.Util

import android.content.Context
import android.content.SharedPreferences

class PeriodoPreferences(context: Context) {
    private var instancia = "PERIODO_PREFACTURA"
    private val preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)


    fun guardarPeriodo(id: Int, idCatalogo: Int,inicio: String,fin: String,fecha_vencimiento:String, concepto: String,estado: String){
        preferences.edit().apply(){
            putInt("id_periodo", id)
            putInt("idCatalogo",idCatalogo)
            putString("periodo_inicio", inicio)
            putString("periodo_fin", fin)
            putString("fecha_vencimiento",fecha_vencimiento)
            putString("concepto", concepto)
            putString("estado", estado)
            apply()
        }
    }

    fun getIdPeriodo(): Int = preferences.getInt("id_periodo",0)?:0
    fun getPeriodoCatalogo(): Int  = preferences.getInt("idCatalogo",0)?:0
    fun getPeriodoInicio(): String  = preferences.getString("periodo_inicio","")?:""
    fun getPeriodoFin(): String = preferences.getString("periodo_fin", "")?: ""
    fun getPeriodoVencimiento(): String = preferences.getString("fecha_vencimiento", "")?: ""
    fun getEstado(): String = preferences.getString("estado","")?: ""
    fun getPeriodoConcepto(): String = preferences.getString("concepto","")?: ""
    fun limpiarPeriodo(){
        preferences.edit().clear().apply()
    }

}