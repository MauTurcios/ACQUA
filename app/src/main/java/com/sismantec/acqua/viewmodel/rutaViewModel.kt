package com.sismantec.acqua.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.entities.RutasEntity

class rutaViewModel (application: Application): AndroidViewModel(application){
    private val db = AppDataBase.obtenerInstancia(application)
    private val rutasDAO = db.RutasDAO()
    val rutas = rutasDAO.obtenerRutas().asLiveData()

    //insertando rutas
    fun insertarRutas (list: List<RutasEntity>){
        viewModelScope.launch {
            rutasDAO.insertarRutas(list)
        }
    }
}