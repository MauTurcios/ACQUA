package com.sismantec.acqua

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sismantec.acqua.databinding.ActivityIdlecturaBinding
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.controller.PeriodoController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Periodo : AppCompatActivity(){
    private var instancia = "CONFIG_SERVIDOR"
    private lateinit var binding: ActivityIdlecturaBinding
    private var funciones = Funciones()
    private var isProcessing  = false
    private var periodoController = PeriodoController()
    private var alert: AlertDialogo?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdlecturaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        alert = AlertDialogo(this@Periodo, this)
        onBackPressedDispatcher.addCallback(this) {}

    }

    override fun onStart(){
        super.onStart()

        binding.btnPeriodo.setOnClickListener {
            iniPeriodo()
        }
    }

    //FUNCION PARA INICIAR EL PERIODO
    private fun iniPeriodo(){
        alert!!.Cargando()
        if(isProcessing) return

        val idPeriodo = binding.txtIdPeriodo.text.toString().trim()

        if (idPeriodo.isEmpty()){
            Toast.makeText(this, "Ingrese un periodo válido", Toast.LENGTH_SHORT).show()
            runOnUiThread {
                alert!!.dismisss()
            }
            return
        }

        isProcessing = true
        binding.txtIdPeriodo.isEnabled = false
        binding.btnPeriodo.isEnabled = false

       lifecycleScope.launch {
           delay(300)
           runOnUiThread {
               alert!!.changeText("OBTENIENDO PERIODO")
           }
           try {
               val periodo = periodoController.obtenerPeriodo(idPeriodo.toInt(),this@Periodo)
               if (periodo){
                   inicio()
               }
           }finally {
               isProcessing = false
               binding.txtIdPeriodo.isEnabled = true
               binding.btnPeriodo.isEnabled = true
           }
           runOnUiThread {
               alert!!.dismisss()
           }
       }
        //Log.d("ID_PERIODO","ID DEL PERIODO: "+idPeriodo)
    }

    private fun inicio(){
        val intent = Intent(this@Periodo, Inicio::class.java)
        startActivity(intent)
        finish()
    }

}