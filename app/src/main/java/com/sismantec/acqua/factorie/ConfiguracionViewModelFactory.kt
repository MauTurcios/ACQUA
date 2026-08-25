package com.sismantec.acqua.factorie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sismantec.acqua.repository.BluetoothRepository
import com.sismantec.acqua.viewmodel.configViewModel
import com.sismantec.acqua.domain.usecase.ObtenerImpresorasVinculadasUseCase

class ConfiguracionViewModelFactory (
    private val bluetoothRepository: BluetoothRepository
): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(configViewModel::class.java)) {
            val useCase = ObtenerImpresorasVinculadasUseCase(bluetoothRepository)
            return configViewModel(bluetoothRepository,useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
