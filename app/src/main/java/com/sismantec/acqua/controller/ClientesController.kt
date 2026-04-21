package com.sismantec.acqua.controller

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.room.Dao
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.Util.SslNoSeguro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import com.sismantec.acqua.apiservices.RetrofitCliente
import com.sismantec.acqua.dao.ClientesDAO
import com.sismantec.acqua.dao.ConfigDAO
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.entities.ConfigEntity
import com.sismantec.acqua.entities.RutasEntity
import com.sismantec.acqua.models.Cliente
import com.sismantec.acqua.viewmodel.clienteViewModel
import com.sismantec.acqua.viewmodel.configViewModel
import com.sismantec.acqua.viewmodel.rutaViewModel
import kotlinx.coroutines.processNextEventInCurrentThread
import java.net.IDN


class ClientesController {
    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var utilidades = SslNoSeguro()
    //private lateinit var viewModel : clienteViewModel
    //private lateinit var rutaViewModel: rutaViewModel
    //private lateinit var configDAO: ConfigDAO

    //OBTENER CLIENTES
    suspend fun obtenerClientes(context: Context , viewModel: clienteViewModel
    ) = withContext(Dispatchers.IO){
        val baseUrl = funciones.obtenerServidor(context)
        val api = RetrofitCliente.obtenerApi(baseUrl)
        //viewModel = ViewModelProvider(context as ViewModelStoreOwner)[clienteViewModel::class.java]
        try {
            val respuesta = api.obtenerClientes()
            if (respuesta.isSuccessful()){
                val listaCliente = respuesta.body()?: emptyList()
                val cliente = listaCliente.map {
                    ClientesEntity(
                        Id = it.Id,
                        Codigo = it.Codigo,
                        Cliente = it.Cliente,
                        Casa = it.Casa,
                        Poligono = it.Poligono,
                        Id_ruta = it.Id_ruta,
                        Codigo_casa = it.Codigo_casa,
                        Direccion = it.Direccion
                    )
                }
                viewModel.insertarClientes(cliente)
            }
        }catch (e: Exception){
            println("ERROR AL OBTENER LAS CLIENTES -> " + e.message)
        }
    }

    suspend fun obtenerRutas(context: Context, rutaViewModel: rutaViewModel
    ) = withContext(Dispatchers.IO){
        val baseUrl = funciones.obtenerServidor(context)
        val api = RetrofitCliente.obtenerApi(baseUrl)
        try {
            val respuesta = api.obtenerRutas()
            if (respuesta.isSuccessful()){
                val listaRutas = respuesta.body()?: emptyList()
                val rutas = listaRutas.map {
                    RutasEntity(
                        Id = it.Id,
                        Ruta = it.Ruta
                    )
                }
                rutaViewModel.insertarRutas(rutas)
            }
        }catch (e: Exception){
            println("ERROR AL OBTENER LAS RUTAS -> " + e.message)
        }
    }

    suspend fun obtenerConfig(context: Context, configViewModel: configViewModel) = withContext(Dispatchers.IO){
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
    }
}

