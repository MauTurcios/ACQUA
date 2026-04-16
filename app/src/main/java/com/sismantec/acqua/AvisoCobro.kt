package com.sismantec.acqua

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.sismantec.acqua.databinding.ActivityAvisoCobroBinding
import com.sismantec.acqua.viewmodel.clienteViewModel

class AvisoCobro : AppCompatActivity() {
    private lateinit var avisoCobro: ActivityAvisoCobroBinding
    private lateinit var clienteVM: clienteViewModel
    private lateinit var txtCodigo: TextView
    private lateinit var txtCliente: TextView
    private lateinit var txtCasa: TextView
    private var idCliente : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        avisoCobro = ActivityAvisoCobroBinding.inflate(layoutInflater)
        setContentView(avisoCobro.root)
        clienteVM = ViewModelProvider(this)[clienteViewModel::class.java]
        txtCodigo = findViewById(R.id.codigoCliente)
        txtCliente = findViewById(R.id.nombreCliente)
        txtCasa = findViewById(R.id.casa)
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
    }

    private fun cargarCliente() {
        clienteVM.obtenerClienteId(idCliente){
                cliente ->
            if(cliente != null) {
                txtCodigo.text = cliente.Codigo
                txtCliente.text = cliente.Cliente
                txtCasa.text = cliente.Casa
            }
        }
    }

}