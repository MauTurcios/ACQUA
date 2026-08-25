package com.sismantec.acqua

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val prefs = getSharedPreferences("CONFIG_SERVIDOR", Context.MODE_PRIVATE)
        val periodo_prefs = getSharedPreferences("PERIODO_PREFACTURA", Context.MODE_PRIVATE)
        val ip = prefs.getString("ip",null)
        val puerto = prefs.getString("puerto", null)
        val empleado = prefs.getString("nombreEmpleado", null)
        val periodo_id = periodo_prefs.getInt("id_periodo",0)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler(Looper.getMainLooper()).postDelayed({
            if(ip.isNullOrEmpty() && puerto.isNullOrEmpty()) {

                val intent = Intent(this, ConexionServidor::class.java)
                startActivity(intent)
                finish()

            }else if(empleado.isNullOrEmpty()){

                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()

            }else if(periodo_id == 0) {
                val intent = Intent  (this, Periodo::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, Inicio::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000)
    }
}