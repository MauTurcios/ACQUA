package com.sismantec.acqua

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.sismantec.acqua.databinding.ActivityMenuClientesBinding
import com.sismantec.acqua.models.Cliente
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.sismantec.acqua.adapter.ClienteAdapter
import com.sismantec.acqua.apiservices.APIServices
import com.sismantec.acqua.controller.ClientesController
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.entities.RutasEntity
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.viewmodel.clienteViewModel
import com.sismantec.acqua.viewmodel.rutaViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.observeOn
import kotlin.collections.emptyList

class MenuClientes : AppCompatActivity() {

    private lateinit var binding: ActivityMenuClientesBinding
    private var proviene : String = ""
    private var alert: AlertDialogo? = null
    private var busqueda: SearchView? = null
    private var atras : ImageButton? = null
    private lateinit var clienteAdapter: ClienteAdapter
    private var ListadoClientes : List<ClientesEntity> = listOf<ClientesEntity>()
    private var preferences: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var dSearch : String? = null
    private var clienteController = ClientesController()
    private var funciones = Funciones()
    private var cargarClientes =""
    private var mainCliente: ConstraintLayout? = null
    private var recicle: RecyclerView? = null
    private lateinit var viewModel : clienteViewModel
    private lateinit var rutasViewModel: rutaViewModel
    var rutaFiltro: Int? = null
    var listaRutas: List<RutasEntity> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        cargarClientes = preferences!!.getString("CargarClientes", "").toString()
        proviene = intent.getStringExtra("proviene").toString()
        mainCliente = findViewById(R.id.mainCliente)
        recicle = binding.listadoClientes
        clienteAdapter = ClienteAdapter()
        viewModel = ViewModelProvider(this)[clienteViewModel::class.java]
        rutasViewModel = ViewModelProvider(this)[rutaViewModel::class.java]
        val imgRutaFiltro = findViewById<ImageView>(R.id.btnFiltro)
        recicle?.adapter = clienteAdapter
        recicle?.layoutManager = LinearLayoutManager(this)

        rutasViewModel.rutas.observe(this){ rutas ->
            listaRutas = rutas
        }

        viewModel.clientes.observe(this){
            lista ->
            //Log.d("CLIENTES", "Lista size: ${lista.size}")
            clienteAdapter.actualizarLista(lista)
        }

        val txtBusqueda = findViewById<TextInputEditText>(R.id.txtBusquedaCliente)
        txtBusqueda.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                clienteAdapter.busquedaFiltro(s.toString(), rutaFiltro)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        lifecycleScope.launch {
            clienteController.obtenerClientes(this@MenuClientes,viewModel)
        }

        imgRutaFiltro.setOnClickListener {
            if (listaRutas.isEmpty()) {
                Toast.makeText(this, "NO HAY RUTAS GUARDADAS", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectRuta = PopupMenu(this, imgRutaFiltro)
            Log.d("RUTAS_DEBUG", "listaRutas: ${listaRutas.size}")
            Log.d("RUTAS_DEBUG", "liveData: ${rutasViewModel.rutas.value?.size}")
            listaRutas.forEachIndexed { index, ruta ->
                selectRuta.menu.add(0, index, index+1, ruta.Ruta)
            }
            selectRuta.menu.add(0,-1,0, "TODAS")
            selectRuta.setOnMenuItemClickListener { item ->
                val rutaSeleccion = if (item.itemId == -1) {
                    null
                }else {
                    listaRutas[item.itemId].Id
                }
                clienteAdapter.busquedaFiltro(
                    txtBusqueda.text.toString(),
                    rutaSeleccion
                )
                true
            }
            selectRuta.show()

        }

        onBackPressedDispatcher.addCallback(this) {}
    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            if(proviene.contains("nuevoAviso")){
                menuAvisoCobro()
            }else{
                menuInicio()
            }
        }
        binding.mainCliente

        binding.lblTituloMenuCliente.text = if(proviene.contains("nuevoAviso")) {
            "CLIENTE (NUEVO AVISO)"
        }else{
            "LISTADO CLIENTES"
        }
    }
    private fun menuAvisoCobro(){
        val intent = Intent(this@MenuClientes, Menu AvisosCobros::class.java)
        startActivity(intent)
        finish()
    }

    private fun menuInicio(){
        val intent = Intent(this@MenuClientes, Inicio::class.java)
        startActivity(intent)
        finish()
    }

    //PARA VER DATOS GENERALES DEL CLIENTE
    private fun datosCliente(){

    }


}

