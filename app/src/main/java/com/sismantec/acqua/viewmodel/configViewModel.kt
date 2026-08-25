package com.sismantec.acqua.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.domain.usecase.ObtenerImpresorasVinculadasUseCase
import com.sismantec.acqua.entities.ConfigEntity
import com.sismantec.acqua.repository.BluetoothRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class configViewModel(
    private val bluetoothRepository: BluetoothRepository,
    private val obtenerImpresorasVinculadasUseCase: ObtenerImpresorasVinculadasUseCase
): ViewModel() {

    private val _listaImpresoras = MutableStateFlow<List<String>>(emptyList())

    val listaImpresoras = _listaImpresoras.asStateFlow()

    private val _bluetoothHabilitado = MutableStateFlow(false)
    //val bluetoothHabilitado = _bluetoothHabilitado.asStateFlow()

    //----------------------------------------------------------
    //Solicita al Caso de Uso la lista de impresoras emparejadas.
    //----------------------------------------------------------

    fun cargarImpresoras() {
        _bluetoothHabilitado.value = bluetoothRepository.isBluetoothEnabled()
        if (_bluetoothHabilitado.value){
            val impresoras = obtenerImpresorasVinculadasUseCase.ejecutar()
            _listaImpresoras.value = impresoras
        }else{
            _listaImpresoras.value = emptyList()
        }
    }
}