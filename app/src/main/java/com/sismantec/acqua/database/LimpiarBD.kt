package com.sismantec.acqua.database

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room

class LimpiarBD {

    //-----------------------------------------
    //Funcion para Limiar (Eliminar) la bd al actualizar
    //-----------------------------------------
    private lateinit var preferences: SharedPreferences
    fun limpiarBdAlActualizar(context: Context){
        Room.databaseBuilder(context,AppDataBase::class.java, "Acae.db")
            .fallbackToDestructiveMigration()
            .build()
    }

}