package com.sismantec.acqua.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.entities.ClientesEntity


class clienteViewModel(application: Application): AndroidViewModel(application) {
    private val db = AppDataBase.obtenerInstancia(application)
    private val clientesDAO = db.clientesDao()
    val clientes = clientesDAO.obtenerClientes().asLiveData()

    //insertando clientes
    fun insertarClientes (list: List<ClientesEntity>){
        viewModelScope.launch {
            clientesDAO.insertarClientes(list)
        }
    }

    fun obtenerClienteId(id: Int, callback: (ClientesEntity?) -> Unit){
        viewModelScope.launch {
            val cliente = clientesDAO.obtenerClienteId(id)
            callback(cliente)
        }
    }
}