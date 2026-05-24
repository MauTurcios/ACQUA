package com.sismantec.acqua

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.addCallback
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sismantec.acqua.apiservices.RetrofitCliente
import com.sismantec.acqua.controller.ImpresionController
import com.sismantec.acqua.databinding.ActivityAvisoCobroBinding
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.models.ConsumoResponse
import com.sismantec.acqua.viewmodel.clienteViewModel
import com.sismantec.acqua.models.DatosAvisoCobro
import com.sismantec.acqua.models.LecturaRequest
import com.sismantec.acqua.viewmodel.lecturaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AvisoCobro : AppCompatActivity() {
    private var alert: AlertDialogo?=null
    private lateinit var avisoCobro: ActivityAvisoCobroBinding
    private lateinit var clienteVM: clienteViewModel
    private lateinit var lecturaVM: lecturaViewModel
    private lateinit var txtCodigo: TextView
    private lateinit var txtCliente: TextView
    private lateinit var txtCasa: TextView
    private lateinit var direccion: TextView
    private var idCliente : Int = 0
    private var impressionController = ImpresionController(this@AvisoCobro)
    private var clienteLectura: ClientesEntity? = null
    private var funciones = Funciones()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alert = AlertDialogo(this@AvisoCobro, this)
        avisoCobro = ActivityAvisoCobroBinding.inflate(layoutInflater)
        setContentView(avisoCobro.root)
        clienteVM = ViewModelProvider(this)[clienteViewModel::class.java]
        lecturaVM = ViewModelProvider(this)[lecturaViewModel::class.java]

        txtCodigo = findViewById(R.id.codigoCliente)
        txtCliente = findViewById(R.id.nombreCliente)
        txtCasa = findViewById(R.id.casa)
        direccion = findViewById(R.id.direccion)
        idCliente = intent.getIntExtra("idCliente", 0)
        Log.d("DATOS", "ID recibido: $idCliente")
        cargarCliente()
        onBackPressedDispatcher.addCallback(this){}
    }

    override fun onStart() {
        super.onStart()
        avisoCobro.btnAtras.setOnClickListener {
            val intent = Intent(this@AvisoCobro, MenuAvisosCobros::class.java )
            startActivity(intent)
            finish()
        }

        avisoCobro.btnEnviar.setOnClickListener {
            if(clienteLectura == null){
                Log.d("AVISO","clienteLectura NULL")
                return@setOnClickListener
            }
            lifecycleScope.launch{
                val cliente = clienteLectura ?: return@launch
                val lecturaActual = avisoCobro.txtLectura.text.toString()

                calcularConsumo(lecturaActual){ consumo ->
                    val lecturaAnterior : String = "" // Este dato lo obtenemos de la respuesta del WS
                    val total = consumo.consumo //temporal

                    val datos = DatosAvisoCobro(
                        cliente = cliente,
                        lecturaActual = consumo.lecturaActual,
                        lecturaAnterior = consumo.lecturaAnterior,
                        consumo = consumo.consumo,
                        total = total
                    )
                    Log.d("AVISO", "id_cliente: ${datos.cliente.Id}")
                    Log.d("AVISO", "cliente: ${datos.cliente.Cliente}")
                    Log.d("AVISO", "codigo: ${datos.cliente.Codigo}")
                    Log.d("AVISO", "direccion: ${datos.cliente.Direccion}")
                    Log.d("AVISO", "Lectura actual: ${datos.lecturaActual}")
                    Log.d("AVISO","Lecetura anterior: ${datos.lecturaAnterior}")
                    Log.d("AVISO", "Consumo: ${datos.consumo}")

                    /* IMPRIMIR TICKET
                    if (ContextCompat.checkSelfPermission(
                        this@AvisoCobro, Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                        ){
                        impressionController.imprimirRecibo(this@AvisoCobro,datos)
                    }else{
                        Log.e("BT", "Permiso BLUETOOTH_CONNECT no concedido")
                    }
                    */
                    lecturaVM.guardarLectura(datos)

                    Log.d("AVISO","ENVIANDO A MenuAvisoCobro")
                    actAvisoCobro()
                }

            }
        }
    }

    private fun calcularConsumo(lectura: String, onResult: (ConsumoResponse) -> Unit) {
        alert!!.Cargando()
        alert!!.changeText("ENVIANDO LECTURA")
        Log.d("AVISO","MOSTRANDO DIALOGO")
        val baseUrl = funciones.obtenerServidor(this)
        val api = RetrofitCliente.obtenerApi(baseUrl)
        lifecycleScope.launch{
            delay(3000)
            alert!!.changeText("ENVIANDO LECTURA")
            try {
                val response = api.obtenerConsumo(
                    LecturaRequest(lectura)
                )
                if (response.isSuccessful){
                    response.body()?.let { consumoResponse ->
                        alert?.dismisss()
                        onResult(consumoResponse)
                    }
                }else   {
                    Log.e("API", "Error: ${response.code()}")
                    alert?.dismisss()
                }
            }catch (e: Exception){
                alert?.dismisss()
                Log.e("API", "Error conexión", e)
            }
        }
    }

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

    private fun actAvisoCobro(){
        Log.d("AVISO","CERRANDO ACTIVIDAD")
        val intent = Intent(this@AvisoCobro, MenuAvisosCobros::class.java)
        startActivity(intent)
        finish()
    }

}