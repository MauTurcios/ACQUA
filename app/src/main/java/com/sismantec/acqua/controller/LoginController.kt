package com.sismantec.acqua.controller

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.sismantec.acqua.apiservices.RetrofitCliente
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.models.LoginModel
import com.sismantec.acqua.models.LogoutModel
import com.sismantec.acqua.models.RespuestaLogin
import com.sismantec.acqua.Util.SessionManager

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

}