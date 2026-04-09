package com.sismantec.acqua

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sismantec.acqua.controller.ImpresionController
import com.sismantec.acqua.databinding.ActivityMenuConfiguracionBinding
import java.io.File
import java.io.FileOutputStream

class MenuConfiguracion : AppCompatActivity() {

    private lateinit var binding: ActivityMenuConfiguracionBinding
    private val instancia = "CONFIG_SERVIDOR"
    private lateinit var preferencias: SharedPreferences
    private var ipServidor: String = ""
    private var puertoServidor: String = ""
    private var nombreImpresor: String = ""
    private var impressionController = ImpresionController(this@MenuConfiguracion)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        ipServidor = preferencias.getString("ip", "").toString()
        puertoServidor = preferencias.getString("puerto", "").toString()
        nombreImpresor = preferencias.getString("impresorIntegrado", "noImpresor").toString()

        //Seteando el Puerto y la Ip del Servidor
        binding.txtip.setText(ipServidor)
        binding.txtip.isEnabled = false
        binding.txtpuerto.setText(puertoServidor)
        binding.txtpuerto.isEnabled = false
        binding.txtImpresor.setText(nombreImpresor)

        //Ocultando Controles de Impresor
        if(preferencias.getString("tipoImpresora", "") == "BT"){
            binding.lyImpresor.visibility = View.GONE
        }

        //ACTIVANDO SWITCH DE IMRPESORES
        binding.swBluetooth.isChecked = preferencias.getString("tipoImpresora", "") == "BT"
        binding.swIntegrada.isChecked = preferencias.getString("tipoImpresora", "") == "INT"

        permisosBluetooth()

        // Recuperar la imagen guardada al iniciar
        val prefs = getSharedPreferences("MisImagenes", MODE_PRIVATE)
        val filePath = prefs.getString("imagenFile", null)

        if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                binding.imgLogoEmpresa.setImageURI(Uri.fromFile(file))
            }
        }else{
            val nombreImagen = "sinlogo"
            val resId = resources.getIdentifier(nombreImagen, "drawable", packageName)

            val drawable = ContextCompat.getDrawable(this, resId)
            binding.imgLogoEmpresa.setImageDrawable(drawable)
        }
        onBackPressedDispatcher.addCallback(this) {}
    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            val intent = Intent(this@MenuConfiguracion, Inicio::class.java)
            startActivity(intent)
            finish()
        }

        //ACTIVANDO LOGICA DE SWITCH DE IMPRESORES
        binding.swBluetooth.setOnCheckedChangeListener { _, isChecked ->
            preferencias.edit {
                remove("tipoImpresora")
                if (isChecked) {
                    binding.swIntegrada.isChecked = false
                    putString("tipoImpresora", "BT")
                    binding.lyImpresor.visibility = View.GONE
                    remove("impresorIntegrado")
                } else {
                    binding.swIntegrada.isChecked = true
                    putString("tipoImpresora", "INT")
                    binding.lyImpresor.visibility = View.VISIBLE
                }
            }

        }

        binding.swIntegrada.setOnCheckedChangeListener { _, isChecked ->
            preferencias.edit {
                remove("tipoImpresora")
                if (isChecked) {
                    binding.swBluetooth.isChecked = false
                    putString("tipoImpresora", "INT")
                    binding.lyImpresor.visibility = View.VISIBLE
                } else {
                    binding.swBluetooth.isChecked = true
                    putString("tipoImpresora", "BT")
                    binding.lyImpresor.visibility = View.GONE
                    remove("impresorIntegrado")
                }
            }

        }

        binding.btnImpresor.setOnClickListener {

            val impresor = binding.txtImpresor.text

            preferencias.edit{
                remove("impresorIntegrado")
                putString("impresorIntegrado", impresor.toString())
            }

            Toast.makeText(this@MenuConfiguracion, "IMPRESOR CONFIGURADO", Toast.LENGTH_SHORT)
                .show()
        }

        binding.btnPruebaImpresion.setOnClickListener @androidx.annotation.RequiresPermission(
            android.Manifest.permission.BLUETOOTH_CONNECT
        ) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                impressionController.imprimirRecibo(this@MenuConfiguracion)
            }
        }

        binding.imgLogoEmpresa.setOnClickListener {
            seleccionarImagen()
        }

    }

    //Funcion para los permisos Bluetooth
    private fun permisosBluetooth() {
        val permissions = when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            }
            else -> {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            }
        }

        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), 1001)
        } else {
            //Toast.makeText(this, "Permisos Bluetooth concedidos ✅", Toast.LENGTH_SHORT).show()
        }
    }

    private fun seleccionarImagen() {
        seleccionarImagenLauncher.launch("image/*") // solo permite imágenes
    }

    // Launcher para seleccionar imagen
    private val seleccionarImagenLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val fileName = "logoEmpresaSeleccionado.jpg" // puedes hacerlo dinámico
                val savedFile = guardarImagenEnInterno(uri, fileName)

                if (savedFile != null) {
                    binding.imgLogoEmpresa.setImageURI(Uri.fromFile(savedFile))

                    // Guardar en SharedPreferences
                    val prefs = getSharedPreferences("MisImagenes", MODE_PRIVATE)
                    prefs.edit { putString("imagenFile", savedFile.absolutePath) }
                }
            }
        }

    // 🔹 Copiar imagen seleccionada a almacenamiento interno
    private fun guardarImagenEnInterno(uri: Uri, fileName: String): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, fileName) // guardado en /data/data/tu.app/files/
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}