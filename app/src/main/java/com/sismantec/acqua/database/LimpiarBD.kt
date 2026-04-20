package com.sismantec.acqua.database

import android.content.Context
import androidx.room.Room

class LimpiarBD {

    //-----------------------------------------
    //Funcion para Limiar (Eliminar) la bd al actualizar
    //-----------------------------------------
    fun limpiarBdAlActualizar(context: Context){
        Room.databaseBuilder(context,AppDataBase::class.java, "Acae.db")
            .fallbackToDestructiveMigration()
            .build()
    }

}