package com.sismantec.acqua

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
    private lateinit var periodo_prefs: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var proviene : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConexionServidorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this) {}
        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        periodo_prefs = getSharedPreferences("PERIODO_PREFACTURA", Context.MODE_PRIVATE)
        proviene = intent.getStringExtra("proviene") ?: ""

        if(proviene=="actualizaConexion") {
            binding.lyEncabezado.isVisible = true
            binding.btnConectarServidor.text = "ACTUALIZAR CONEXION"

            //MOSTRAR DATOS ACTUALES DE CONEXION
            binding.txtIpServidor.setText(preferences.getString("ip", ""))
            binding.txtPuertoServidor.setText(preferences.getString("puerto", ""))

        }else {
            binding.lyEncabezado.isVisible = false
            binding.btnConectarServidor.text = "CONECTAR"
            Log.d("CONFIG_SERVER","VERIFICANDO SERVIDOR")
            verificarServidor()
        }
    }

    override fun onStart() {
        super.onStart()

        binding.btnConectarServidor.setOnClickListener {
            if(proviene =="actualizaConexion") {
                actualizaConexionServidor()
            }else{
                conexionServidor()
            }
        }

        binding.btnAtras.setOnClickListener {
            val intent = Intent(this@ConexionServidor, MenuConfiguracion::class.java)
            startActivity(intent)
            finish()
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


    private fun actualizaConexionServidor(){
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

                            //LOG ANTES
                            Log.d("CONFIG_SERVER","CONEXION ANTES: ${preferences.getString("ip", "")}, ${preferences.getString("puerto","")}")

                            limpiarPrefs()

                            //LOG DESPUES
                            Log.d("CONFIG_SERVER","CONEXION DESPUES: ${preferences.getString("ip", "")}, ${preferences.getString("puerto","")}")

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

    fun limpiarPrefs() {
        preferences.edit().clear().apply()
        periodo_prefs.edit().clear().apply()
    }




}