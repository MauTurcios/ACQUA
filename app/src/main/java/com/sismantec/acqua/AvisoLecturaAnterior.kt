package com.sismantec.acqua

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.autofill.ImageTransformation
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sismantec.acqua.Util.PeriodoPreferences
import com.sismantec.acqua.apiservices.RetrofitCliente
import com.sismantec.acqua.controller.ImpresionController
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.databinding.ActivityAvisoCobroBinding
import com.sismantec.acqua.databinding.ActivityLecturaAnteriorBinding
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.models.ConsumoResponse
import com.sismantec.acqua.models.LecturaAnteriorRequest
import com.sismantec.acqua.viewmodel.clienteViewModel
import com.sismantec.acqua.models.LecturaRequest
import com.sismantec.acqua.viewmodel.lecturaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor

class AvisoLecturaAnterior: AppCompatActivity() {
    private var alert: AlertDialogo?=null
    private lateinit var binding: ActivityLecturaAnteriorBinding
    //private lateinit var clienteVM: clienteViewModel
    private lateinit var lecturaVM: lecturaViewModel
    private var funciones = Funciones()
    private val instancia = "CONFIG_SERVIDOR"
    private lateinit var preferencias: SharedPreferences
    private lateinit var periodo_prefs: PeriodoPreferences
    private var cuenta: String = ""
    private var idPeriodo: Int = 0
    private var actividad: String =""
    private var idLecturaDetalle: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturaAnteriorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        iniVar()
        onBackPressedDispatcher.addCallback(this){}
    }

    private fun iniVar(){
        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        periodo_prefs = PeriodoPreferences(this@AvisoLecturaAnterior)
        alert = AlertDialogo(this@AvisoLecturaAnterior, this)
        cuenta = intent.getStringExtra("cuenta").toString()
        actividad = intent.getStringExtra("actividad").toString()
        lecturaVM = ViewModelProvider(this)[lecturaViewModel::class.java]
        idPeriodo = periodo_prefs.getIdPeriodo()
        idLecturaDetalle = intent.getIntExtra("Id_lectura_detalle", 0)
    }

    override fun onStart() {
        super.onStart()
        binding.btnAtras.setOnClickListener {
            if (actividad.contains("AVISO_COBRO")){
                actProcesarLectura()
            }else{
                actDetalleLectura()
            }
        }
        binding.txtInfoCuenta.setText("LECTURA ANTERIOR DE LA CUENTA: $cuenta")

        binding.btnEnviar.setOnClickListener {
            val lecturaTxt = binding.txtLectura.text.toString().trim().toDoubleOrNull()
            if (lecturaTxt == null){
                binding.lyLectura.error = "Ingrese una lectura"
                return@setOnClickListener
            }
            binding.lyLectura.error = null
            procesarLecturaAnterior(cuenta,lecturaTxt,idPeriodo)
        }
        Log.d("PROVIENE",actividad)
    }

    private fun procesarLecturaAnterior(cuenta: String, lAnterior: Double, idLectura: Int){
        alert!!.Cargando()
        val baseUrl = funciones.obtenerServidor(this)
        val api = RetrofitCliente.obtenerApi(baseUrl)
        lifecycleScope.launch {
            val hayInternet = funciones.isInternetAvailable(this@AvisoLecturaAnterior)
            runOnUiThread {
                alert!!.changeText("ENVIANDO LECTURA")
            }
            if (hayInternet) {
                try {
                    val response = api.procesarLecturaAnterior(
                        LecturaAnteriorRequest(
                            cuenta = cuenta,
                            idLectura = idLectura,
                            lectura_anterior = lAnterior
                        )
                    )
                    if (response.isSuccessful){
                        runOnUiThread {
                            alert!!.changeText("LECTURA PROCESADA CON EXITO")
                        }
                        delay(1500)
                        runOnUiThread {
                            alert?.dismisss()
                        }
                        if (actividad.contains("AVISO_COBRO")){
                            actProcesarLectura()
                        }else{
                            actDetalleLectura()
                        }
                    }else{
                        val mensaje = response.errorBody()
                            ?.string()?.trim()?.removeSurrounding("\"")
                            ?: "Error al procesar la lectura."
                        Log.e("API", " [PROCESAR_LECTURA_ANTERIOR]HTTP ${response.code()}: $mensaje")
                        runOnUiThread {
                            alert?.dismisss()
                            Toast.makeText(this@AvisoLecturaAnterior, mensaje, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        alert?.dismisss()
                        Toast.makeText(
                            this@AvisoLecturaAnterior,
                            "Error de conexión con el servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Log.e("API", "[PROCESAR_LECTURA_ANTERIOR CATCH] Error conexión", e)
                }
            } else {
                runOnUiThread {
                    alert?.dismisss()
                    Toast.makeText(
                        this@AvisoLecturaAnterior,
                        "No tiene conexión a internet",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("API", "[PROCESAR_LECTURA_ANTERIOR ELSE] Error conexión")
                }
            }
        }
    }

    private fun actProcesarLectura(){
        Log.d("AVISO","CERRANDO ACTIVIDAD")
        val intent = Intent(this@AvisoLecturaAnterior, AvisoCobro::class.java)
        startActivity(intent)
        finish()
    }

    private fun actDetalleLectura(){
        Log.d("DETALLE","CERRANDO ACTIVIDAD")
        val intent = Intent(this@AvisoLecturaAnterior, AvisoCobroDetalle::class.java)
        intent.putExtra("Id_lectura_detalle", idLecturaDetalle)
        startActivity(intent)
        finish()
    }

}