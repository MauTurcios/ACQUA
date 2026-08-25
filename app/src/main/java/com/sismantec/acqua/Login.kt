package com.sismantec.acqua

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sismantec.acqua.controller.ClientesController
import com.sismantec.acqua.controller.LoginController
import com.sismantec.acqua.databinding.ActivityLoginBinding
import com.sismantec.acqua.funciones.Funciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var periodo_prefs: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var periodo_instancia = "PERIODO_PREFACTURA"
    private var nombreEmpresa: String = ""
    private var id_periodo: Int =0
    private var funciones = Funciones()

    private var isProcessing  = false
    private var loginController = LoginController()
    private var clientesController = ClientesController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        periodo_prefs = getSharedPreferences(periodo_instancia, Context.MODE_PRIVATE)
        nombreEmpresa = preferences.getString("dteNombreComercial", "").toString()
        id_periodo = periodo_prefs.getInt("id_periodo",0)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this) {}
    }

    override fun onStart() {
        super.onStart()

        binding.btnIngresar.setOnClickListener {
            iniciarSesion()
        }

    }

    //Funcion para Iniciar Sesion
    private fun iniciarSesion(){

        if(isProcessing) return

        isProcessing = true
        binding.btnIngresar.isEnabled = false
        binding.txtUsuario.isEnabled = false
        binding.txtContrasena.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val usuario = binding.txtUsuario.text.toString()
        val password = binding.txtContrasena.text.toString()
        lifecycleScope.launch(Dispatchers.IO) {

            val hayInternet = funciones.isInternetAvailable(this@Login)

            withContext(Dispatchers.Main){

                if(hayInternet){
                    if(usuario.isEmpty() || password.isEmpty()){
                        Toast.makeText(this@Login, "LOS CAMPOS SON REQUEDIOS", Toast.LENGTH_SHORT)
                            .show()
                    }else{

                        val respuesta = loginController.iniciarSesion(usuario, password, this@Login)
                        when(respuesta!!.respuestaServidor){
                            "CREDENCIALES_VALIDAS" -> {
                                if(nombreEmpresa.isNullOrEmpty()) {
                                    Log.d("CONFIG", "NO EXISTE EMPRESA:  $nombreEmpresa")
                                    try {
                                        loginController.obtenerConfig(this@Login)
                                    } catch (e: Exception) {
                                        println("ERROR AL CARGAR CONFIG" + e.message)
                                    }
                                }
                                if (id_periodo ==0){
                                    periodo()
                                }else{
                                    Log.d("CONFIG", "EXISTE EMPRESA:  $nombreEmpresa")
                                    Log.d("CONFIG","EXISTE PERIODO: ID $id_periodo")
                                    inicio()
                                }
                            }
                            "CREDENCIALES_INVALIDAS" -> {
                                Toast.makeText(this@Login, "DATOS INCORRECTOS", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            else -> {
                                Toast.makeText(this@Login, respuesta.respuestaServidor, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                    }
                }else{
                    Toast.makeText(this@Login, "NO TIENE CONEXION A INTERNET", Toast.LENGTH_SHORT)
                        .show()
                }

            }

            withContext(Dispatchers.Main){
                isProcessing = false
                binding.btnIngresar.isEnabled = true
                binding.txtUsuario.isEnabled = true
                binding.txtContrasena.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }

        }

    }

    //Funcion para redireccionar a Salones
    private fun inicio(){
        val intent = Intent(this@Login, Inicio::class.java)
        startActivity(intent)
        finish()
    }

    private fun periodo(){
        val intent = Intent(this@Login, Periodo::class.java)
        startActivity(intent)
        finish()
    }

}