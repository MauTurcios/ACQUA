package com.sismantec.acqua.apiservices

import com.sismantec.acqua.models.Cliente
import com.sismantec.acqua.models.ConfigModel
import com.sismantec.acqua.models.LoginModel
import com.sismantec.acqua.models.LogoutModel
import com.sismantec.acqua.models.RespuestaConexion
import com.sismantec.acqua.models.RespuestaLogin
import com.sismantec.acqua.models.RespuestaLogout
import com.sismantec.acqua.models.RutasModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIServices {

    //Conexion con el Servidor
    @GET("conexion")
    suspend fun conexionServidor() : RespuestaConexion

    //Funciones para el Controllador del Loguin
    @Headers("Content-Type: application/json")
    @POST("login")
    suspend fun iniciarSesion(
        @Body request: LoginModel
    ) : Response<RespuestaLogin>

    @Headers("Content-Type: application/json")
    @POST("logout")
    suspend fun cerrarSesion(
        @Body request: LogoutModel
    ) : Response<RespuestaLogout>

    //Obtener clientes
    @GET("clientes")
    suspend fun obtenerClientes(): Response<List<Cliente>>
    //Obtener rutas
    @GET("rutas")
    suspend fun obtenerRutas(): Response<List<RutasModel>>

    @GET("config")
    suspend fun obtenerConfig() : Response<List<ConfigModel>>
}