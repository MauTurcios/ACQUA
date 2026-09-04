package com.sismantec.acqua.database

import android.content.Context

class LimpiarBD {

    //-----------------------------------------
    //Funcion para Limiar (Eliminar) la bd al actualizar
    //-----------------------------------------
    fun limpiarBdAlActualizar(context: Context): Boolean{
        AppDataBase.cerrarInstancia()
        return context.applicationContext.deleteDatabase(AppDataBase.DATABASE_NAME)
    }
}