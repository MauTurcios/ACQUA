package com.sismantec.acqua.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

class BluetoothRepository {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()


    //----------------------------------------------------------
    //Obtiene la lista de dispositivos Bluetooth que ya han sido vinculados
    //----------------------------------------------------------
    @SuppressLint("MissingPermission")
    fun obtenerDispositivosVinculados(): List<BluetoothDevice>{
        return if (bluetoothAdapter != null && bluetoothAdapter.isEnabled){
            bluetoothAdapter.bondedDevices.toList()
        }else{
            emptyList()
        }
    }

    //----------------------------------------------------------
    //Verifica si el Bluetooth esta encendido.
    //----------------------------------------------------------
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled ?: false
    }
}