package com.sismantec.acqua

import android.content.Intent
import android.os.Bundle
import android.app.Dialog
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import com.sismantec.acqua.databinding.ActivityCargaDatosBinding
import  com.sismantec.acqua.controller.ClientesController
import com.sismantec.acqua.dao.ConfigDAO
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.viewmodel.clienteViewModel
import com.sismantec.acqua.viewmodel.configViewModel
import com.sismantec.acqua.viewmodel.rutaViewModel
import kotlinx.coroutines.coroutineScope
import okhttp3.Dispatcher


class CargaDatos : AppCompatActivity() {

    private lateinit var binding: ActivityCargaDatosBinding
    private lateinit var url : String
    private var alert: AlertDialogo?=null
    private var instancia = "CONFIG_SERVIDOR"
    private var clientesController = ClientesController()
    private var funciones = Funciones()

    private lateinit var preferences: SharedPreferences
    private lateinit var db : AppDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCargaDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDataBase.obtenerInstancia(this@CargaDatos)
        preferences = this@CargaDatos.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        alert = AlertDialogo(this@CargaDatos, this)
        url = funciones.obtenerServidor(context = this@CargaDatos)
        onBackPressedDispatcher.addCallback(this) {}

    }

    override fun onStart() {
        super.onStart()

        binding.imgbtnatras.setOnClickListener {
            regresar()
        }

        binding.btnCargarClientes.setOnClickListener {
            lifecycleScope.launch {
                if (funciones.isInternetAvailable(this@CargaDatos)){
                    cargarClientes()
                }else  {
                    funciones.mostrarAlerta("NO TIENES CONEXION A INTERNET", this@CargaDatos, binding.main)
                }
            }
        }

    }


    //FUNCION PARA CARGAR CLIENTES
    private fun cargarClientes(){
        alert!!.Cargando()
        val viewModel = ViewModelProvider(this)[clienteViewModel::class.java]
        val rutaViewModel = ViewModelProvider(this)[rutaViewModel::class.java]
        val configViewModel = ViewModelProvider(this)[configViewModel::class.java]
        lifecycleScope.launch{
            delay(300)
            alert!!.changeText("CARGANDO CLIENTES")
            try {
                clientesController.obtenerClientes(this@CargaDatos, viewModel)
            }catch (e: Exception){
                println("ERROR AL CARGAR INFORMACION DE CLIENTES ->"+e.message)
            }
            alert!!.changeText("CARGANDO RUTAS")
            try {
                clientesController.obtenerRutas(this@CargaDatos, rutaViewModel)
            }catch (e: Exception){
                println("ERROR AL CARGAR RUTAS" + e.message)
            }
            //PRUEBA DE CARGA DE LA CONFIG
            alert!!.changeText("CARGANDO CONFIG")
            try {
                clientesController.obtenerConfig(this@CargaDatos,configViewModel)
            }catch (e: Exception){
                println("ERROR AL CARGAR CONFIG" + e.message)
            }
            //FIN DE CARGA DE LA CONFIG
            alert!!.changeText("INFORMACIÓN CARGADA CORRECTAMENTE")
            delay(300)
            alert!!.dismisss()
        }
    }

    private fun regresar(){
        val intent = Intent(this@CargaDatos, Inicio::class.java)
        startActivity(intent)
        finish()
    }


}