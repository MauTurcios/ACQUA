package com.sismantec.acqua.database

import android.content.Context

class LimpiarBD {

    //-----------------------------------------
    //Funcion para Limpiar (Eliminar) la BD al actualizar
    //-----------------------------------------
    fun limpiarBdAlActualizar(context: Context): Boolean {
        AppDataBase.cerrarInstancia()
        return context.applicationContext.deleteDatabase(AppDataBase.DATABASE_NAME)
    }
}
