package com.sismantec.acqua.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.models.DatosAvisoCobro
import com.sismantec.acqua.models.LecturaResumen
import kotlinx.coroutines.launch

class lecturaViewModel (application: Application): AndroidViewModel(application) {

    private val db = AppDataBase.obtenerInstancia(application)
    private val lecturaDAO = db.LecturaDAO()

    fun guardarLectura(datos: DatosAvisoCobro){
        viewModelScope.launch {
            val lectura = LecturaEntity(
                Id_cliente = datos.cliente.Id,
                Lectura_anterior = datos.lecturaAnterior,
                Lectura_actual = datos.lecturaActual,
                Consumo = datos.consumo
            )
            lecturaDAO.insertar(lectura)
        }
    }

    fun obtenerLecturas(callback: (List<LecturaResumen>) -> Unit){
        viewModelScope.launch {
            val lista = lecturaDAO.obtenerLecturasResumen()
            callback(lista)
        }
    }

    fun borrarAvisoCobro(){
        viewModelScope.launch {
            lecturaDAO.borrarAvisos()
        }
    }
}