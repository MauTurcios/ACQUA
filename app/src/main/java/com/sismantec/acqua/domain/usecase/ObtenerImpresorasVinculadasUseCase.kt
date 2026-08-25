package com.sismantec.acqua.domain.usecase

import com.sismantec.acqua.repository.BluetoothRepository
import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass

class ObtenerImpresorasVinculadasUseCase (
    private val repository: BluetoothRepository
){
    @SuppressLint("MissingPermission")
    fun ejecutar(): List<String>{
        val dispositivos = repository.obtenerDispositivosVinculados()

        // Filtramos para devolver solo los nombres de dispositivos tipo IMAGING (Impresoras)
        return dispositivos
            .filter { device ->
                val majorClass = device.bluetoothClass?.majorDeviceClass
                majorClass == BluetoothClass.Device.Major.IMAGING ||
                        device.name?.lowercase()?.contains("print") == true ||
                        device.name?.lowercase()?.contains("printer") == true
            }
            .map { it.name?: "Desconocido" }
            .distinct()
            .sorted()
    }
}