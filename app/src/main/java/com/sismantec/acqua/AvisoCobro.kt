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
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.models.ConsumoResponse
import com.sismantec.acqua.viewmodel.clienteViewModel
import com.sismantec.acqua.models.LecturaRequest
import com.sismantec.acqua.viewmodel.lecturaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AvisoCobro : AppCompatActivity() {
    private var alert: AlertDialogo?=null
    private lateinit var binding: ActivityAvisoCobroBinding
    //private lateinit var clienteVM: clienteViewModel
    private lateinit var lecturaVM: lecturaViewModel
    //private lateinit var txtCodigo: TextView
    //private lateinit var txtCliente: TextView
    //private lateinit var txtCasa: TextView
    //private lateinit var direccion: TextView
    //private var idCliente : Int = 0
    private lateinit var impressionController: ImpresionController
    //private var clienteLectura: ClientesEntity? = null
    private var funciones = Funciones()

    private lateinit var periodo_prefs: PeriodoPreferences
    private var periodo_concepto: String = ""
    private var idPeriodo: Int = 0
    private val instancia = "CONFIG_SERVIDOR"
    private var vendedor : String = ""
    private lateinit var preferencias: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alert = AlertDialogo(this@AvisoCobro, this)
        binding = ActivityAvisoCobroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lecturaVM = ViewModelProvider(this)[lecturaViewModel::class.java]
        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        vendedor = preferencias.getString("nombreEmpleado", "").toString()
        /*
        clienteVM = ViewModelProvider(this)[clienteViewModel::class.java]
        idCliente = intent.getIntExtra("idCliente", 0)
        Log.d("DATOS", "ID recibido: $idCliente")
         */
        impressionController = ImpresionController(this@AvisoCobro)
        periodo_prefs = PeriodoPreferences(this@AvisoCobro)
        onBackPressedDispatcher.addCallback(this){}
        periodo_concepto = periodo_prefs.getPeriodoConcepto()
        idPeriodo = periodo_prefs.getIdPeriodo()
    }

    override fun onStart() {
        super.onStart()
        binding.txtInfoPeriodo.text = periodo_concepto
        binding.btnAtras.setOnClickListener {
            val intent = Intent(this@AvisoCobro, MenuAvisosCobros::class.java )
            startActivity(intent)
            finish()
        }

        binding.btnEnviar.setOnClickListener {
            // VALIDAR QUE CUENTA NO SE UN CAMPO VACÍO
            val cuenta = binding.txtClienteLectura.text.toString().trim()
            if (cuenta.isEmpty()){
                binding.lyClienteLectura.error = "Ingrese la cuenta"
                return@setOnClickListener
            }
            binding.lyClienteLectura.error = null

            //VALIDAR QUE LECTURA NO SEA UN CAMPO VACÍO
            val lecturaTxt = binding.txtLectura.text.toString().trim().toDoubleOrNull()
            if (lecturaTxt == null){
                binding.lyLectura.error = "Ingrese una lectura"
                return@setOnClickListener
            }
            binding.lyLectura.error = null

            //VALIDAR SI LA CUENTA YA TIENE UNA LECTURA REGISTRADA EN LA APP
            lecturaVM.existeLecturaPendiente(cuenta){
                existePendiente ->
                if (existePendiente){
                        Toast.makeText(
                            this@AvisoCobro,
                            "Ya existe una lectura pendiente de enviar para esta cuenta",
                            Toast.LENGTH_LONG
                        ).show()
                    return@existeLecturaPendiente
                }
                //SI LA CUENTA NO TIENE UNA LECTURA PENDIENTE REGISTRADA
                procesarLectura(cuenta,lecturaTxt,idPeriodo,vendedor){
                        consumo ->
                    Log.d("AVISO", "Cuenta: ${consumo.cuenta}")
                    Log.d("AVISO", "Periodo: ${consumo.periodo}")
                    Log.d("AVISO", "Lectura anterior: ${consumo.lecturaAnterior}")
                    Log.d("AVISO", "Lectura actual: ${consumo.lecturaActual}")
                    Log.d("AVISO", "Consumo: ${consumo.consumo}")

                    lecturaVM.guardarLectura(consumo)

                    Log.d("AVISO","ENVIANDO A MenuAvisoCobro")
                    actAvisoCobro()
                }
            }

        }
    }

    private fun procesarLectura(cuenta: String, lectura: Double, idLectura: Int, empleado: String, onResult: (ConsumoResponse) -> Unit) {
        alert!!.Cargando()
        val baseUrl = funciones.obtenerServidor(this)
        val api = RetrofitCliente.obtenerApi(baseUrl)
        lifecycleScope.launch{
            val hayInternet = funciones.isInternetAvailable(this@AvisoCobro)
            runOnUiThread {
                alert!!.changeText("ENVIANDO LECTURA")
            }
            if (hayInternet) {
                try {
                    val response = api.procesarLectura(
                        LecturaRequest(
                            cuenta = cuenta,
                            idLectura = idLectura,
                            lectura_actual = lectura,
                            empleado = empleado
                        )
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { consumoResponse ->
                            delay(1500)
                            runOnUiThread {
                                alert!!.changeText("LECTURA PROCESADA CON EXITO")
                            }
                            delay(1500)
                            runOnUiThread {
                                alert?.dismisss()
                            }
                            onResult(consumoResponse)
                        }

                        //IMPRIMIR TICKET
                        val consumo = response.body()
                        if (consumo != null){
                            impressionController.imprimirRecibo(this@AvisoCobro,consumo!!)
                        }
                    } else {
                        val mensaje = response.errorBody()
                            ?.string()?.trim()?.removeSurrounding("\"")
                            ?: "Error al procesar la lectura."
                        Log.e("API", " [PROCESAR LECTURA]HTTP ${response.code()}: $mensaje")
                        runOnUiThread {
                            alert?.dismisss()
                            Toast.makeText(this@AvisoCobro, mensaje, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        alert?.dismisss()
                        Toast.makeText(
                            this@AvisoCobro,
                            "Error de conexión con el servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Log.e("API", "[PROCESAR LECTURA] Error conexión", e)
                }
            }else{
                alert?.dismisss()
                mensajeLecturaPendiente(
                    cuenta = cuenta,
                    periodo = idLectura,
                    lectura = lectura,
                    usuario = empleado
                )
                Log.e("API", "[PROCESAR LECTURA] Error conexión")
            }
        }
    }

    /*
    private fun cargarCliente() {
        clienteVM.obtenerClienteId(idCliente){
                cliente ->
            if(cliente != null) {
                clienteLectura = cliente
                clienteLectura = cliente
                txtCodigo.text = cliente.Codigo
                txtCliente.text = cliente.Cliente
                txtCasa.text = cliente.Casa
                direccion.text = cliente.Direccion
            }
        }
    }
     */


    private fun mensajeLecturaPendiente(cuenta: String, lectura: Double, periodo: Int, usuario: String){
        val lecturaDialog = Dialog(this, R.style.Theme_Dialog)
        lecturaDialog.setCancelable(false)

        lecturaDialog.setContentView(R.layout.diallog_guardar_lectura)

        val guardarLectura = lecturaDialog.findViewById<TextView>(R.id.guardarLectura)
        val cancelLectura = lecturaDialog.findViewById<TextView>(R.id.cancelLectura)

        guardarLectura.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val lecturaPendiente = LecturaEntity(
                    Cuenta = cuenta,
                    Nombre = "",
                    Periodo = periodo,
                    Lectura_anterior = 0.0,
                    Lectura_actual = lectura,
                    Consumo = 0,
                    Usuario = usuario,
                    Documento = "",
                    Direccion = "",
                    IdColbar = 0,
                    Colbar = "",
                    IdSector = 0,
                    Sector = "",
                    IdZona = 0,
                    Zona = "",
                    Lectura_enviada = false,
                    Cf1_linea = "",
                    Cf1_descripcion = "",
                    Cf1_precio = 0.00,
                    Cf2_linea = "",
                    Cf2_descripcion = "",
                    Cf2_precio = 0.00,
                    Cf3_linea = "",
                    Cf3_descripcion = "",
                    Cf3_precio = 0.00,
                    Cf4_linea = "",
                    Cf4_descripcion = "",
                    Cf4_precio = 0.00,
                    Cf5_linea = "",
                    Cf5_descripcion = "",
                    Cf5_precio = 0.00
                )
                AppDataBase.obtenerInstancia(this@AvisoCobro).LecturaDAO().insertar(lecturaPendiente)
                withContext(Dispatchers.Main){
                    lecturaDialog.dismiss()
                        Toast.makeText(
                            this@AvisoCobro,
                            "Lectura almacenada correctamente",
                            Toast.LENGTH_LONG
                        ).show()
                    actAvisoCobro()
                }
            }
        }
        cancelLectura.setOnClickListener {
            lecturaDialog.dismiss()
        }
        lecturaDialog.show()
    }

    private fun actAvisoCobro(){
        Log.d("AVISO","CERRANDO ACTIVIDAD")
        val intent = Intent(this@AvisoCobro, MenuAvisosCobros::class.java)
        startActivity(intent)
        finish()
    }

}