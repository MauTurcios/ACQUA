package com.sismantec.acqua

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.sismantec.acqua.controller.ImpresionController
import com.sismantec.acqua.databinding.ActivityAvisoCobroBinding
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.viewmodel.clienteViewModel
import com.sismantec.acqua.models.DatosAvisoCobro

class AvisoCobro : AppCompatActivity() {
    private lateinit var avisoCobro: ActivityAvisoCobroBinding
    private lateinit var clienteVM: clienteViewModel
    private lateinit var txtCodigo: TextView
    private lateinit var txtCliente: TextView
    private lateinit var txtCasa: TextView
    private lateinit var direccion: TextView
    private var idCliente : Int = 0
    private var impressionController = ImpresionController(this@AvisoCobro)
    private var clienteLectura: ClientesEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        avisoCobro = ActivityAvisoCobroBinding.inflate(layoutInflater)
        setContentView(avisoCobro.root)
        clienteVM = ViewModelProvider(this)[clienteViewModel::class.java]

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

        avisoCobro.btnEnviar.setOnClickListener{
            if(clienteLectura == null){
                Log.d("AVISO","clienteLectura NULL")
                return@setOnClickListener
            }
            val cliente = clienteLectura ?: return@setOnClickListener
            val lecturaActual = avisoCobro.txtLectura.text.toString().toIntOrNull() ?: 0
            val lecturaAnterior = 0   // este dato se obtendrá desde la base de datos
            val consumo = lecturaActual - lecturaAnterior
            val total = consumo    // temporal

            val datos = DatosAvisoCobro(
                cliente = cliente,
                lecturaActual = lecturaActual,
                lecturaAnterior = lecturaAnterior,
                consumo = consumo,
                total = total
            )
            Log.d("AVISO", "cliente: ${datos.cliente.Cliente}")
            Log.d("AVISO", "codigo: ${datos.cliente.Codigo}")
            Log.d("AVISO", "direccion: ${datos.cliente.Direccion}")
            Log.d("AVISO", "Lectura: ${datos.lecturaActual}")

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

}