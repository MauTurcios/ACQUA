package com.sismantec.acqua

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.sismantec.acqua.adapter.LecturaAdapter
import com.sismantec.acqua.databinding.ActivityMenuAvisosCobrosBinding
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.models.LecturaResumen
import com.sismantec.acqua.viewmodel.lecturaViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

class MenuAvisosCobros : AppCompatActivity() {

    private lateinit var binding: ActivityMenuAvisosCobrosBinding
    private lateinit var lecturaAdapter: LecturaAdapter
    private var listadoAvisosCobros: List<LecturaResumen> = listOf()
    private var preferences: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var mainAvisosCobros: ConstraintLayout? = null
    private var recicle: RecyclerView? = null
    private lateinit var lecturasViewModel: lecturaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuAvisosCobrosBinding.inflate(layoutInflater)
        iniVar()
        observersVM()
        setContentView(binding.root)
        mostrarAvisos()
        onBackPressedDispatcher.addCallback(this) {}
    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            val intent = Intent(this@MenuAvisosCobros, Inicio::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnNuevoAviso.setOnClickListener {
            val intent = Intent(this@MenuAvisosCobros, MenuClientes::class.java)
            intent.putExtra("proviene", "nuevoAviso")
            startActivity(intent)
            finish()
        }

    }
    override fun onResume() {
        super.onResume()
        observersVM()
    }
    private fun iniVar(){
        lecturasViewModel = ViewModelProvider(this)[lecturaViewModel::class.java]
    }

    private fun mostrarAvisos() {
        lecturaAdapter = LecturaAdapter(listadoAvisosCobros)

        binding.listadoClientes.apply {
            layoutManager = LinearLayoutManager(this@MenuAvisosCobros)
            adapter = lecturaAdapter
        }
    }
    private fun observersVM() {

        lecturasViewModel.obtenerLecturas { lista ->

            listadoAvisosCobros = lista
            lecturaAdapter.actualizar(lista)

        }
    }

}