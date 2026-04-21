package com.sismantec.acqua

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.databinding.ActivityDatosClienteBinding
import com.sismantec.acqua.databinding.ActivityMenuClientesBinding
import com.sismantec.acqua.models.Cliente
import com.sismantec.acqua.viewmodel.clienteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatosCliente: AppCompatActivity() {
    private lateinit var biding: ActivityDatosClienteBinding
    private lateinit var viewModel: clienteViewModel
    private lateinit var txtCodigo: TextView
    private lateinit var txtCliente: TextView
    private lateinit var txtCasa: TextView
    private lateinit var txtDireccion: TextView
    private var idCliente : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        biding = ActivityDatosClienteBinding.inflate(layoutInflater)
        setContentView(biding.root)
        //setContentView(R.layout.activity_datos_cliente)
        //private val db = AppDataBase.obtenerInstancia(application)
        txtCodigo = findViewById(R.id.txtCodigo)
        txtCliente = findViewById(R.id.txtCliente)
        txtCasa = findViewById(R.id.txtCasa)
        txtDireccion = findViewById(R.id.txtDireccion)
        viewModel = ViewModelProvider(this)[clienteViewModel::class.java]
        idCliente = intent.getIntExtra("idCliente", 0)
        Log.d("DATOS", "ID recibido: $idCliente")
        cargarCliente()
        onBackPressedDispatcher.addCallback(this){}
    }

    override fun onStart() {
        super.onStart()
        biding.btnAtras.setOnClickListener {
            val intent = Intent(this@DatosCliente, MenuClientes::class.java)
            startActivity(intent)
            finish()
        }
    }
/*
    private fun menuClientes(){
        val intent = Intent(this@DatosCliente, MenuClientes::class.java)
        startActivity(intent)
        finish()
    }*/

    private fun cargarCliente() {
        viewModel.obtenerClienteId(idCliente){
            cliente ->
            if(cliente != null) {
                txtCodigo.text = cliente.Codigo
                txtCliente.text = cliente.Cliente
                txtCasa.text = cliente.Casa
                txtDireccion.text = cliente.Direccion

            }
        }
    }
}