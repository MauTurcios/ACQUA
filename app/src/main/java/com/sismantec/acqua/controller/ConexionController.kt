package com.sismantec.acqua.controller

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.sismantec.acqua.apiservices.RetrofitCliente
import com.sismantec.acqua.funciones.Funciones

class ConexionController {

    private var funciones = Funciones()

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"


    //Funcion para validar los campos
    fun validarDatosConexion(ip: String, puerto: String) : Boolean{
        var validos = true
        if(ip.isEmpty() or puerto.isEmpty()){
            validos = false
        }
        return validos
    }

    //Funcion para Conectar con el Servidor
    suspend fun conectarServidor(ip: String, puerto: String) : String{

        val servidor = funciones.servidor(ip, puerto)

        val api = RetrofitCliente.obtenerApi(servidor)

        var respuestaServidor : String = ""

        try {

            val respuesta = api.conexionServidor()

            respuestaServidor = respuesta.respuesta.toString()

        }catch (e: Exception){

            respuestaServidor = "ERROR_CONEXION"

        }
        return respuestaServidor

    }

    //Funcion Almacenar Servidor
    fun almacenarServidor(ip: String, puerto: String, context: Context){

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        preferences.edit{
            putString("ip", ip)
            putString("puerto", puerto)
            putString("tipoImpresora", "BT")
            putString("impresorIntegrado", "sinNombre")
        }

    }

}