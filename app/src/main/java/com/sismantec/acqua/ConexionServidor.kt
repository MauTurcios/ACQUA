package com.sismantec.acqua

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sismantec.acqua.controller.ConexionController
import com.sismantec.acqua.databinding.ActivityConexionServidorBinding
import com.sismantec.acqua.funciones.Funciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConexionServidor : AppCompatActivity() {


    private lateinit var binding : ActivityConexionServidorBinding
    private val conexionController = ConexionController()
    private val funciones = Funciones()
    private var isProcessing  = false

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConexionServidorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this) {}

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        verificarServidor()

    }

    override fun onStart() {
        super.onStart()

        binding.btnConectarServidor.setOnClickListener {
            conexionServidor()
        }

    }

    //Verificar Conexion almacenada
    private fun verificarServidor(){
        if(preferences.contains("puerto") && preferences.contains("ip")){

            val idEmpleado = preferences.getInt("idEmpleado", 0)

            if(idEmpleado > 0){
                inicio()
            }else{
                login()
            }
        }
    }

    //FUNCION PARA CONEXION CON EL SERVIDOR
    private fun conexionServidor(){

        if (isProcessing) return

        val ip = binding.txtIpServidor.text.toString()
        val puerto = binding.txtPuertoServidor.text.toString()

        isProcessing = true
        binding.btnConectarServidor.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {

            val hayInternet = funciones.isInternetAvailable(this@ConexionServidor)

            withContext(Dispatchers.Main){

                if(hayInternet){

                    if(conexionController.validarDatosConexion(ip, puerto)){

                        val respuesta = conexionController.conectarServidor(ip, puerto)
                        if(respuesta.contains("CONEXION_EXITOSA")){
                            conexionController.almacenarServidor(ip, puerto,this@ConexionServidor)
                            Toast.makeText(this@ConexionServidor, "CONEXION EXITOSA CON EL SERVIDOR", Toast.LENGTH_SHORT)
                                .show()
                            login()
                        }else{
                            Toast.makeText(this@ConexionServidor, "ERROR AL CONEXION CON EL SERVIDOR", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }else{

                        Toast.makeText(this@ConexionServidor, "DATOS INCORRECTOS EN LA CONEXION", Toast.LENGTH_SHORT)
                            .show()

                    }
                }else{
                    Toast.makeText(this@ConexionServidor, "NO TIENE CONEXION A INTERNET", Toast.LENGTH_SHORT)
                        .show()
                }

                isProcessing = false
                binding.btnConectarServidor.isEnabled = true
                binding.progressBar.visibility = View.GONE

            }

        }
    }

    //Funcion para redireccionar al Login
    private fun login(){
        val intent = Intent(this@ConexionServidor, Login::class.java)
        startActivity(intent)
        finish()
    }

    //Funcion para redireccion a la pantalla de Salones
    private fun inicio() {
        val intent = Intent(this@ConexionServidor, Inicio::class.java)
        startActivity(intent)
        finish()
    }





}