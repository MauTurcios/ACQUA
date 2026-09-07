package com.sismantec.acqua

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.sismantec.acqua.Util.PeriodoPreferences
import com.sismantec.acqua.databinding.ActivityLecturaDetalleBinding
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.viewmodel.lecturaViewModel
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.sismantec.acqua.apiservices.RetrofitCliente
import com.sismantec.acqua.controller.ImpresionController
import com.sismantec.acqua.models.LecturaRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.sismantec.acqua.models.ConsumoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    private var isProcessing  = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnEnviarPendiente.isVisible = false
        binding.btnImprimirAviso.isVisible = false
        iniVar()
        cargarDatosLectura()
        Log.d("INFO_PERIODO", "ID RECIBIDO: $idLecturaDetalle")
        binding.txtPeriodo.setText(periodo_concepto)
        onBackPressedDispatcher.addCallback(this) {}
    }

    override fun onStart() {
        super.onStart()
        iniVar()
        cargarDatosLectura()
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

    //FUNCION PARA INICIALIZAR VARIABLES
    private fun iniVar() {
        lecturaVM = ViewModelProvider(this)[lecturaViewModel::class.java]
        periodo_prefs = PeriodoPreferences(this@AvisoCobroDetalle)
        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        periodo_concepto = periodo_prefs.getPeriodoConcepto()
        idPeriodo = periodo_prefs.getIdPeriodo()
        vendedor = preferencias.getString("nombreEmpleado", "").toString()
        alert = AlertDialogo(this@AvisoCobroDetalle, this)
        impresionController = ImpresionController(this@AvisoCobroDetalle)
        idLecturaDetalle = intent.getIntExtra("Id_lectura_detalle", 0)
    }

    //FUNCION QUE ENVÍA A LA ACTIVIDAD MenuAvisosCobros
    private fun actAvisoCobro() {
        val intent = Intent(this@AvisoCobroDetalle, MenuAvisosCobros::class.java)
        startActivity(intent)
        finish()
    }

    //FUNCION PARA CARGA DE DATOS
    private fun cargarDatosLectura(onCargada: () -> Unit ={}) {
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

                onCargada()
            } else {
                Log.e("LECTURA_DETALLE", "No se encontró la lectura: $idLecturaDetalle")
            }
        }
    }

    //FUNCION PARA PROCESAR LA LECTURA
    private fun procesarLecturaPendiente(lectura: LecturaEntity) {
        if(isProcessing) return

        isProcessing = true
        binding.btnImprimirAviso.isEnabled = false
        binding.btnEnviarPendiente.isEnabled = false
        alert!!.Cargando()
        val baseUrl = funciones.obtenerServidor(this)
        val api = RetrofitCliente.obtenerApi(baseUrl)

        lifecycleScope.launch {
            try {
                val hayInternet = funciones.isInternetAvailable(this@AvisoCobroDetalle)
                alert!!.changeText("ENVIANDO LECTURA")

                if (!hayInternet) {
                    Toast.makeText(
                        this@AvisoCobroDetalle,
                        "No hay conexión a Internet",
                        Toast.LENGTH_LONG
                    ).show()
                    habilitarOpcion()
                    return@launch
                }
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
                        alert!!.changeText("LECTURA PROCESADA CON EXITO")
                        delay(1500)
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
                            //CARGOS FIJOS
                            cf1_linea = consumoResponse.cf1_linea,
                            cf1_descripcion = consumoResponse.cf1_descripcion,
                            cf1_precio = consumoResponse.cf1_precio,
                            cf2_linea = consumoResponse.cf2_linea,
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
                            cf5_precio = consumoResponse.cf5_precio,
                            //PLIEGO TARIFARIO
                            minimo_m3 = consumoResponse.minimo_m3,
                            primeros_m3 = consumoResponse.primeros_m3,
                            primeros_m3_valor = consumoResponse.primeros_m3_valor,
                            e1_minimo_m3 = consumoResponse.e1_minimo_m3,
                            e1_maximo_m3 = consumoResponse.e1_maximo_m3,
                            e1_valor_m3 = consumoResponse.e1_valor_m3,
                            e2_minimo_m3 = consumoResponse.e2_minimo_m3,
                            e2_maximo_m3 = consumoResponse.e2_maximo_m3,
                            e2_valor_m3 = consumoResponse.e2_valor_m3,
                            e3_minimo_m3 = consumoResponse.e3_minimo_m3,
                            e3_maximo_m3 = consumoResponse.e3_maximo_m3,
                            e3_valor_m3 = consumoResponse.e3_valor_m3,
                            e4_minimo_m3 = consumoResponse.e4_minimo_m3,
                            e4_maximo_m3 = consumoResponse.e4_maximo_m3,
                            e4_valor_m3 = consumoResponse.e4_valor_m3,
                            e5_minimo_m3 = consumoResponse.e5_minimo_m3,
                            e5_maximo_m3 = consumoResponse.e5_maximo_m3,
                            e5_valor_m3 = consumoResponse.e5_valor_m3,
                            e6_minimo_m3 = consumoResponse.e6_minimo_m3,
                            e6_maximo_m3 = consumoResponse.e6_maximo_m3,
                            e6_valor_m3 = consumoResponse.e6_valor_m3,
                            e7_minimo_m3 = consumoResponse.e7_minimo_m3,
                            e7_maximo_m3 = consumoResponse.e7_maximo_m3,
                            e7_valor_m3 = consumoResponse.e7_valor_m3,
                            e8_minimo_m3 = consumoResponse.e8_minimo_m3,
                            e8_maximo_m3 = consumoResponse.e8_maximo_m3,
                            e8_valor_m3 = consumoResponse.e8_valor_m3,
                            e9_minimo_m3 = consumoResponse.e9_minimo_m3,
                            e9_maximo_m3 = consumoResponse.e9_maximo_m3,
                            e9_valor_m3 = consumoResponse.e9_valor_m3,
                            e10_minimo_m3 = consumoResponse.e10_minimo_m3,
                            e10_maximo_m3 = consumoResponse.e10_maximo_m3,
                            e10_valor_m3 = consumoResponse.e10_valor_m3
                        ) {
                            cargarDatosLectura {
                                alert?.dismisss()
                                imprimiendo = true
                                impresionController.imprimirRecibo(
                                    this@AvisoCobroDetalle,
                                    consumoResponse
                                )
                                habilitarOpcion()
                            }
                        }
                    }
                } else {
                    val mensaje = response.errorBody()
                        ?.string()?.trim()?.removeSurrounding("\"")
                        ?: "Error al procesar la lectura."
                    Log.e("API", " [PROCESAR LECTURA]HTTP ${response.code()}: $mensaje")
                    Toast.makeText(this@AvisoCobroDetalle, mensaje, Toast.LENGTH_LONG).show()
                    habilitarOpcion()
                    if (mensaje == "ULTIMA_LECTURA_REQUERIDA") {
                        mensajeLecturaAnterior(
                            cuenta = lectura.Cuenta
                        )
                    }
                }
            }catch (e: Exception) {
                Log.e("REENVIAR_LECTURA", "Error conexión", e)
                Toast.makeText(
                    this@AvisoCobroDetalle,
                    "Error de conexión con el servidor",
                    Toast.LENGTH_LONG
                ).show()
                habilitarOpcion()
            }
        }
    }

    //FUNCION PARA IMPRIMIR LA LECTURA YA PROCESADA
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
            cf5_precio = lectura.Cf5_precio,
            //PLIEGO TARIFARIO
            minimo_m3 = lectura.Minimo_m3,
            primeros_m3 = lectura.Primeros_m3,
            primeros_m3_valor = lectura.Primeros_m3_valor,
            e1_minimo_m3 = lectura.E1_minimo_m3,
            e1_maximo_m3 = lectura.E1_maximo_m3,
            e1_valor_m3 = lectura.E1_valor_m3,
            e2_minimo_m3 = lectura.E2_minimo_m3,
            e2_maximo_m3 = lectura.E2_maximo_m3,
            e2_valor_m3 = lectura.E2_valor_m3,
            e3_minimo_m3 = lectura.E3_minimo_m3,
            e3_maximo_m3 = lectura.E3_maximo_m3,
            e3_valor_m3 = lectura.E3_valor_m3,
            e4_minimo_m3 = lectura.E4_minimo_m3,
            e4_maximo_m3 = lectura.E4_maximo_m3,
            e4_valor_m3 = lectura.E4_valor_m3,
            e5_minimo_m3 = lectura.E5_minimo_m3,
            e5_maximo_m3 = lectura.E5_maximo_m3,
            e5_valor_m3 = lectura.E5_valor_m3,
            e6_minimo_m3 = lectura.E6_minimo_m3,
            e6_maximo_m3 = lectura.E6_maximo_m3,
            e6_valor_m3 = lectura.E6_valor_m3,
            e7_minimo_m3 = lectura.E7_minimo_m3,
            e7_maximo_m3 = lectura.E7_maximo_m3,
            e7_valor_m3 = lectura.E7_valor_m3,
            e8_minimo_m3 = lectura.E8_minimo_m3,
            e8_maximo_m3 = lectura.E8_maximo_m3,
            e8_valor_m3 = lectura.E8_valor_m3,
            e9_minimo_m3 = lectura.E9_minimo_m3,
            e9_maximo_m3 = lectura.E9_maximo_m3,
            e9_valor_m3 = lectura.E9_valor_m3,
            e10_minimo_m3 = lectura.E10_minimo_m3,
            e10_maximo_m3 = lectura.E10_maximo_m3,
            e10_valor_m3 = lectura.E10_valor_m3
        )
        impresionController.imprimirRecibo(this@AvisoCobroDetalle,consmo){
            binding.btnImprimirAviso.postDelayed({
                imprimiendo = false
                binding.btnImprimirAviso.isEnabled = true
            },2000)
        }

    }

    //FUNCION QUE MUESTRA EL MENSAJE EN CASO NO EXISTA LECTURA ANTERIOR
    private fun mensajeLecturaAnterior(cuenta: String){
        val lAnteriorDialog = Dialog(this,R.style.Theme_Dialog)
        lAnteriorDialog.setCancelable(false)
        lAnteriorDialog.setContentView(R.layout.dialog_lectura_anterior)

        val procesarAnterior = lAnteriorDialog.findViewById<TextView>(R.id.procesarAnterior)
        val cancelLectura = lAnteriorDialog.findViewById<TextView>(R.id.cancelLectura)

        procesarAnterior.setOnClickListener {
            actLecturaAnterior(cuenta)
            lAnteriorDialog.dismiss()
        }
        cancelLectura.setOnClickListener {
            lAnteriorDialog.dismiss()
        }
        lAnteriorDialog.show()
    }

    //FUNCION QUE ENVÍA A LA ACTIVIDAD AvisoLecturaAnterior
    private fun actLecturaAnterior(cuenta: String){
        Log.d("AVISO","CERRANDO ACTIVIDAD")
        val intent = Intent(this@AvisoCobroDetalle, AvisoLecturaAnterior::class.java)
        intent.putExtra("cuenta",cuenta)
        intent.putExtra("actividad","DETALLE_AVISO")
        intent.putExtra("Id_lectura_detalle", idLecturaDetalle)
        startActivity(intent)
        finish()
    }

    private fun habilitarOpcion(){
        alert!!.dismisss()
        isProcessing = false
        imprimiendo = false
        binding.btnImprimirAviso.isEnabled = true
        binding.btnEnviarPendiente.isEnabled = true
    }
}