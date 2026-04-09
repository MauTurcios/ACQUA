package com.sismantec.acqua

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sismantec.acqua.databinding.ActivityAvisoCobroBinding

class AvisoCobro : AppCompatActivity() {
    private lateinit var avisoCobro: ActivityAvisoCobroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        avisoCobro = ActivityAvisoCobroBinding.inflate(layoutInflater)
        setContentView(avisoCobro.root)
        onBackPressedDispatcher.addCallback(this){}
    }

    override fun onStart() {
        super.onStart()
        avisoCobro.btnAtras.setOnClickListener {
            val intent = Intent(this@AvisoCobro, MenuAvisosCobros::class.java )
        }
    }

}