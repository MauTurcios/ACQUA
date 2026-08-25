package com.sismantec.acqua.controller

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.sismantec.acqua.apiservices.RetrofitCliente
import com.sismantec.acqua.entities.ConfigEntity
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.models.LoginModel
import com.sismantec.acqua.models.LogoutModel
import com.sismantec.acqua.models.RespuestaLogin
import com.sismantec.acqua.models.ConfigModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    suspend fun iniciarSesion(usuario: String, password: String, context: Context) : RespuestaLogin? {

        var respuesta : RespuestaLogin? = null

        try {
            val servidor = funciones.obtenerServidor(context)
            val api = RetrofitCliente.obtenerApi(servidor)
            val request = LoginModel(usuario.trim(), password.toString())

            val response = api.iniciarSesion(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body!!.respuestaServidor == "CREDENCIALES_VALIDAS") {
                    respuesta = RespuestaLogin(
                        idEmpleado = body.idEmpleado,
                        nombreEmpleado = body.nombreEmpleado,
                        respuestaServidor = body.respuestaServidor
                    )

                    almacenarSesionUsuario(respuesta, context)
                    //SessionManager(context).guardarUsuario(respuesta.idEmpleado, respuesta.nombreEmpleado)
                }
            } else {
                respuesta = RespuestaLogin(
                    idEmpleado = 0,
                    nombreEmpleado = "",
                    respuestaServidor = "CREDENCIALES_INCORRECTAS"
                )
            }
        } catch (e: Exception) {
            respuesta = RespuestaLogin(
                idEmpleado = 0,
                nombreEmpleado = "",
                respuestaServidor = "ERROR_CONEXION -> ${e.message}"
            )
        }
        return respuesta
    }

    suspend fun cerrarSesion(idVendedor: Int, context: Context) : String {

        var respuesta : String = ""

        try {
            val servidor = funciones.obtenerServidor(context)
            val api = RetrofitCliente.obtenerApi(servidor)
            val request = LogoutModel(idVendedor)

            val response = api.cerrarSesion(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body!!.respuesta == "LOGOUT_EXITOSO") {
                    respuesta = "LOGOUT_EXITOSO"
                    eliminarSesionUsuario(context)
                    //SessionManager(context).logout()
                }
            } else {
                respuesta = "LOGOUT_ERROR"
            }
        } catch (e: Exception) {
            respuesta = "LOGOUT_ERROR " + e.message
        }
        return respuesta
    }

    suspend fun obtenerConfig(context: Context) = withContext(Dispatchers.IO){
        val baseUrl = funciones.obtenerServidor(context)
        val api = RetrofitCliente.obtenerApi(baseUrl)
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        try {
            val respuesta = api.obtenerConfig()
            if(respuesta.isSuccessful()){
                val config = respuesta.body()?: emptyList()
                Log.d("CONFIG", "Cantidad registros: ${config.size}")
                if (config.isNotEmpty()){
                    Log.d("CONFIG", config.first().toString())
                    almacenarConfig(config.first(),context)
                    val prefs = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
                    Log.d(
                        "CONFIG",
                        "Leido despues de guardar: ${
                            prefs.getString("dteNombreComercial", "")
                        }"
                    )
                }
            }
        }catch (e: Exception){
            println("ERROR AL OBTENER LA CONFIGURACIÓN -> " + e.message)
        }
    }

    private fun almacenarSesionUsuario(obj : RespuestaLogin, context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        preferences.edit{
            putInt("idEmpleado", obj.idEmpleado)
            putString("nombreEmpleado", obj.nombreEmpleado.trim())
        }

    }

    private fun eliminarSesionUsuario(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        preferences.edit{
            remove("idEmpleado")
            remove("nombreEmpleado")
        }
    }

    private fun almacenarConfig(config: ConfigModel, context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        preferences.edit{
            putString("dtePais", config.dtePais)
            putString("dteDepto", config.dteDepto)
            putString("dteMunicipio", config.dteMunicipio)
            putString("dteDistrito", config.dteDistrito)
            putString("dteNit", config.dteNit)
            putString("dteNrc", config.dteNrc)
            putString("dteNombreEmisor", config.dteNombreEmisor)
            putString("dteGiro", config.dteGiro)
            putString("dteNombreComercial", config.dteNombreComercial)
            putString("dteDireccion", config.dteDireccion)
            putString("dteTelefono", config.dteTelefono)
            putString("dteCorreo", config.dteCorreo)
        }
        Log.d(
            "CONFIG",
            "Leído después de guardar: ${
                preferences.getString("dteNombreComercial", "")
            }"
        )
    }

/*
        suspend fun obtenerConfig_(context: Context, configViewModel: configViewModel) = withContext(Dispatchers.IO){
        val baseUrl = funciones.obtenerServidor(context)
        val api = RetrofitCliente.obtenerApi(baseUrl)
        try {
            val respuesta = api.obtenerConfig()
            if(respuesta.isSuccessful()){
                val config = respuesta.body()?: emptyList()
                val conf = config.map {
                    ConfigEntity(
                        Id = it.Id,
                        dtePais = it.dtePais,
                        dteDepto = it.dteDepto,
                        dteMunicipio = it.dteMunicipio,
                        dteDistrito = it.dteDistrito,
                        dteNit = it.dteNit,
                        dteNrc = it.dteNrc,
                        dteEmisor = it.dteNombreEmisor,
                        dteGiro = it.dteGiro,
                        dteNombreComercial = it.dteNombreComercial,
                        dteDireccion = it.dteDireccion,
                        dteTelefono = it.dteTelefono,
                        dteCorreo = it.dteCorreo
                    )
                }
                configViewModel.insertarConfig(conf)
            }
        }catch (e: Exception){
            println("ERROR AL OBTENER LA CONFIGURACIÓN -> " + e.message)
        }

 */
    }


