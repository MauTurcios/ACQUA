package com.sismantec.acqua.Util

import android.content.Context

class SessionManager(context: Context) {
    private var instancia = "CONFIG_SERVIDOR"
    private val shared = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
    //GUARDAR USUSARIO EN SHARED PREFERENCES AL INICIAR SESIÓN
    fun guardarUsuario(id: Int, nombre: String){
        shared.edit()
            .putInt("idVendedor", id)
            .putString("nombreVendedor", nombre)
            .apply()
    }
    //OBTENER ID DEL EMPLEADO
    fun getId(): Int {
        return shared.getInt("idVendedor", 0)
    }
    //OBTENER NOMBRE DEL EMPLEADO
    fun getNombre(): String {
        return shared.getString("nombreVendedor", "") ?: ""
    }
    //ELIMINAR DATOS AL CERRAR SESIÓN
    fun logout(){
        shared.edit().clear().apply()
    }
}