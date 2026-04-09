package com.sismantec.acqua.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.entities.ConfigEntity

class configViewModel(application: Application): AndroidViewModel(application) {
    private val db = AppDataBase.obtenerInstancia(application)
    private val configDAO = db.ConfigDAO()
    val config = configDAO.obtenerConfig().asLiveData()

    fun insertarConfig (list: List<ConfigEntity>){
        viewModelScope.launch {
            configDAO.insertarConfig(list)
        }
    }
}