package com.sismantec.acqua

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sismantec.acqua.controller.LoginController
import com.sismantec.acqua.databinding.ActivityLoginBinding
import com.sismantec.acqua.funciones.Funciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private var funciones = Funciones()

    private var isProcessing  = false
    private var loginController = LoginController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
                                inicio()
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

}