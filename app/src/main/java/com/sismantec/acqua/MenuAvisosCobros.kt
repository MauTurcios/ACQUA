package com.sismantec.acqua

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sismantec.acqua.databinding.ActivityMenuAvisosCobrosBinding

class MenuAvisosCobros : AppCompatActivity() {

    private lateinit var binding: ActivityMenuAvisosCobrosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuAvisosCobrosBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

}