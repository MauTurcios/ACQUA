package com.sismantec.acqua

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.sismantec.acqua.Util.PeriodoPreferences
import com.sismantec.acqua.controller.LoginController
import com.sismantec.acqua.databinding.ActivityInicioBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Inicio : AppCompatActivity() {

    private lateinit var binding: ActivityInicioBinding
    private val instancia = "CONFIG_SERVIDOR"
    private val periodo_instancia = "PERIODO_PREFACTURA"
    private lateinit var preferencias: SharedPreferences
    private lateinit var periodo_prefs: SharedPreferences
    private var vendedor : String = ""
    private var idVendedor : Int = 0
    private var logincontroller = LoginController()
    private var periodo_inicio: String = ""
    private var periodo_fin: String = ""
    private var periodo_concepto: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        binding.imgConfigMovil.isVisible = false
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this) {}
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        periodo_prefs = getSharedPreferences(periodo_instancia, MODE_PRIVATE)
        vendedor = preferencias.getString("nombreEmpleado", "").toString()
        idVendedor = preferencias.getInt("idEmpleado", 0).toInt()
        periodo_inicio = periodo_prefs.getString("periodo_inicio", "").toString()
        periodo_fin = periodo_prefs.getString("periodo_fin", "").toString()
        periodo_concepto = periodo_prefs.getString("concepto","").toString()
    }

    override fun onStart() {
        super.onStart()

        binding.cvCargarDatos.setOnClickListener {
            cargarDatos()
        }

        binding.cvAvisos.setOnClickListener {
            menuAvisoCobro()
        }

        binding.cvConfig.setOnClickListener {
            menuConfiguracion()
        }

        binding.cvSalir.setOnClickListener {
            mensajeConfirmacion()
        }

        binding.lblEmpleado.text = vendedor

        binding.lblPeriodo.text = periodo_concepto

        /*binding.imgConfigMovil.setOnClickListener {
            menuConfiguracion()
        }*/

    }

    private fun menuConfiguracion(){
        val intent = Intent(this@Inicio, MenuConfiguracion::class.java)
        startActivity(intent)
        finish()
    }

    private fun cargarDatos(){
        val intent = Intent(this@Inicio, CargaDatos::class.java)
        startActivity(intent)
        finish()
    }

    private fun menuClientes(){
        val intent = Intent(this@Inicio, MenuClientes::class.java)
        startActivity(intent)
        finish()
    }

    private fun menuAvisoCobro(){
        val intent = Intent(this@Inicio, MenuAvisosCobros::class.java)
        startActivity(intent)
        finish()
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeConfirmacion(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA CERRAR SESION?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                cerrarSesion()
            }
            .setNegativeButton("CANCELAR"){view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //FUNCION PARA CERRAR SESION
    private fun cerrarSesion(){

        lifecycleScope.launch(Dispatchers.IO) {
            val respuesta = logincontroller.cerrarSesion(idVendedor, this@Inicio)
            println("RESPUESTA DEL SERVIDOR -> " + respuesta)
            if(respuesta.contains("LOGOUT_EXITOSO")){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@Inicio, "SESION FINALIZADA CON EXITO", Toast.LENGTH_SHORT)
                        .show()

                    login()
                }
            }else{
                withContext(Dispatchers.Main){
                    Toast.makeText(this@Inicio, "ERROR AL FINALIZAR LA SESION", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun login(){
        val intent = Intent(this@Inicio, Login::class.java)
        startActivity(intent)
        finish()
    }


}