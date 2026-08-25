package com.sismantec.acqua

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.sismantec.acqua.Util.PeriodoPreferences
import com.sismantec.acqua.databinding.ActivityLecturaDetalleBinding
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.viewmodel.lecturaViewModel
import android.app.Dialog
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.sismantec.acqua.apiservices.RetrofitCliente
import com.sismantec.acqua.controller.ImpresionController
import com.sismantec.acqua.models.LecturaRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.sismantec.acqua.factorie.ConfiguracionViewModelFactory
import com.sismantec.acqua.models.ConsumoResponse
import com.sismantec.acqua.repository.BluetoothRepository
import com.sismantec.acqua.viewmodel.configViewModel

class AvisoCobroDetalle: AppCompatActivity() {
    private lateinit var binding: ActivityLecturaDetalleBinding
    private lateinit var lecturaVM: lecturaViewModel
    private var funciones = Funciones()
    private lateinit var periodo_prefs: PeriodoPreferences
    private var periodo_concepto: String = ""
    private var idLecturaDetalle: Int = 0
    private var idPeriodo: Int = 0
    private val instancia = "CONFIG_SERVIDOR"
    private var vendedor: String = ""
    private lateinit var preferencias: SharedPreferences
    private var lecturaDetalle: LecturaEntity? = null
    private var alert: AlertDialogo?=null
    private lateinit var impresionController: ImpresionController
    private var imprimiendo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnEnviarPendiente.isVisible = false
        binding.btnImprimirAviso.isVisible = false
        iniVar()
        idLecturaDetalle = intent.getIntExtra("Id_lectura_detalle", 0)
        cargarDatosLectura()
        Log.d("INFO_PERIODO", "ID RECIBIDO: $idLecturaDetalle")
        binding.txtPeriodo.setText(periodo_concepto)
        onBackPressedDispatcher.addCallback(this) {}
    }

    override fun onStart() {
        super.onStart()
        binding.btnAtras.setOnClickListener {
            actAvisoCobro()
        }
        binding.btnEnviarPendiente.setOnClickListener {
            lecturaDetalle?.let { lectura ->
                procesarLecturaPendiente(lectura)
            }
        }
        binding.btnImprimirAviso.setOnClickListener {
            if (imprimiendo){
                return@setOnClickListener
            }
            lecturaDetalle?.let {
                lectura ->
                imprimiendo = true
                binding.btnImprimirAviso.isEnabled = false
                imprimirLectura(lectura)
            }
        }

    }

    private fun iniVar() {
        lecturaVM = ViewModelProvider(this)[lecturaViewModel::class.java]
        periodo_prefs = PeriodoPreferences(this@AvisoCobroDetalle)
        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        periodo_concepto = periodo_prefs.getPeriodoConcepto()
        idPeriodo = periodo_prefs.getIdPeriodo()
        vendedor = preferencias.getString("nombreEmpleado", "").toString()
        alert = AlertDialogo(this@AvisoCobroDetalle, this)
        impresionController = ImpresionController(this@AvisoCobroDetalle)
    }

    private fun actAvisoCobro() {
        val intent = Intent(this@AvisoCobroDetalle, MenuAvisosCobros::class.java)
        startActivity(intent)
        finish()
    }

    private fun cargarDatosLectura() {
        lecturaVM.obtenerLecturaId(idLecturaDetalle) { lectura ->
            if (lectura != null) {

                lecturaDetalle = lectura

                binding.txtCuenta.setText(lectura.Cuenta)
                binding.txtClienteLec.setText(lectura.Nombre)
                binding.txtDireccionLec.setText(lectura.Direccion)
                binding.txtSector.setText(lectura.Sector)
                binding.txtZona.setText(lectura.Zona)
                binding.txtAnterior.setText(lectura.Lectura_anterior.toString())
                binding.txtActual.setText(lectura.Lectura_actual.toString())
                binding.txtConsumo.setText(lectura.Consumo.toString())
                Log.d("LECTURA_DETALLE", "Leectura encontrada: $idLecturaDetalle")

                binding.btnEnviarPendiente.visibility =
                    if (lectura.Lectura_enviada == false) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                binding.btnImprimirAviso.visibility =
                    if (lectura.Lectura_enviada == true){
                        View.VISIBLE
                    }else{
                        View.GONE
                    }
            } else {
                Log.e("LECTURA_DETALLE", "No se encontró la lectura: $idLecturaDetalle")
            }
        }
    }

    private fun procesarLecturaPendiente(lectura: LecturaEntity) {
        alert!!.Cargando()
        val baseUrl = funciones.obtenerServidor(this)
        val api = RetrofitCliente.obtenerApi(baseUrl)

        lifecycleScope.launch {
            val hayInternet = funciones.isInternetAvailable(this@AvisoCobroDetalle)
            runOnUiThread {
                alert!!.changeText("ENVIANDO LECTURA")
            }
            if (!hayInternet) {
                runOnUiThread {
                    alert?.dismisss()
                    Toast.makeText(
                        this@AvisoCobroDetalle,
                        "No hay conexión a Internet",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@launch
            }
            try {
                val response = api.procesarLectura(
                    LecturaRequest(
                        cuenta = lectura.Cuenta,
                        idLectura = lectura.Periodo,
                        lectura_actual = lectura.Lectura_actual,
                        empleado = lectura.Usuario
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
                        lecturaVM.marcarLecturaEnviada(
                            id = lectura.id,
                            nombre = consumoResponse.nombre,
                            lecturaAnterior = consumoResponse.lecturaAnterior,
                            lecturaActual = consumoResponse.lecturaActual,
                            consumo = consumoResponse.consumo,
                            usuario = consumoResponse.usuario,
                            documento = consumoResponse.documento,
                            direccion = consumoResponse.direccion,
                            idcolbar = consumoResponse.idColbar,
                            colbar = consumoResponse.colbar,
                            idsector = consumoResponse.idSector,
                            sector = consumoResponse.sector,
                            idzona = consumoResponse.idZona,
                            zona = consumoResponse.zona,
                            cf1_linea = consumoResponse.cf1_linea,
                            cf1_descripcion = consumoResponse.cf1_descripcion,
                            cf1_precio = consumoResponse.cf1_precio,
                            cf2_linea = consumoResponse.cf1_linea,
                            cf2_descripcion = consumoResponse.cf2_descripcion,
                            cf2_precio = consumoResponse.cf2_precio,
                            cf3_linea = consumoResponse.cf3_linea,
                            cf3_descripcion = consumoResponse.cf3_descripcion,
                            cf3_precio = consumoResponse.cf3_precio,
                            cf4_linea = consumoResponse.cf4_linea,
                            cf4_descripcion = consumoResponse.cf4_descripcion,
                            cf4_precio = consumoResponse.cf4_precio,
                            cf5_linea = consumoResponse.cf5_linea,
                            cf5_descripcion = consumoResponse.cf5_descripcion,
                            cf5_precio = consumoResponse.cf5_precio
                        ) {
                            cargarDatosLectura()
                            impresionController.imprimirRecibo(this@AvisoCobroDetalle,consumoResponse)
                        }
                    }
                } else {
                    val mensaje = response.errorBody()
                        ?.string()?.trim()?.removeSurrounding("\"")
                        ?: "Error al procesar la lectura."
                    Log.e("API", "HTTP ${response.code()}: $mensaje")
                    runOnUiThread {
                        alert?.dismisss()
                        Toast.makeText(this@AvisoCobroDetalle, mensaje, Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("REENVIAR_LECTURA", "Error conexión", e)
                runOnUiThread {
                    alert?.dismisss()
                    Toast.makeText(
                        this@AvisoCobroDetalle,
                        "Error de conexión con el servidor",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun imprimirLectura(lectura: LecturaEntity){
        val consmo = ConsumoResponse(
            cuenta = lectura.Cuenta,
            nombre = lectura.Nombre,
            periodo = lectura.Periodo,
            lecturaAnterior = lectura.Lectura_anterior,
            lecturaActual = lectura.Lectura_actual,
            consumo = lectura.Consumo,
            appPrefacturado = lectura.Lectura_enviada,
            usuario = lectura.Usuario,
            documento = lectura.Documento,
            direccion = lectura.Direccion,
            idColbar = lectura.IdColbar,
            colbar = lectura.Colbar,
            idSector = lectura.IdSector,
            sector = lectura.Sector,
            idZona = lectura.IdZona,
            zona = lectura.Zona,
            cf1_linea = lectura.Cf1_linea,
            cf1_descripcion = lectura.Cf1_descripcion,
            cf1_precio = lectura.Cf1_precio,
            cf2_linea = lectura.Cf2_linea,
            cf2_descripcion = lectura.Cf2_descripcion,
            cf2_precio = lectura.Cf2_precio,
            cf3_linea = lectura.Cf3_linea,
            cf3_descripcion = lectura.Cf3_descripcion,
            cf3_precio = lectura.Cf3_precio,
            cf4_linea = lectura.Cf4_linea,
            cf4_descripcion = lectura.Cf4_descripcion,
            cf4_precio = lectura.Cf4_precio,
            cf5_linea = lectura.Cf5_linea,
            cf5_descripcion = lectura.Cf5_descripcion,
            cf5_precio = lectura.Cf5_precio
        )
        impresionController.imprimirRecibo(this@AvisoCobroDetalle,consmo){
            binding.btnImprimirAviso.postDelayed({
                imprimiendo = false
                binding.btnImprimirAviso.isEnabled = true
            },2000)
        }

    }

}