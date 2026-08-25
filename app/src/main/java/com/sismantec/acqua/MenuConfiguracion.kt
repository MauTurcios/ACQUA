package com.sismantec.acqua

import android.Manifest
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.sismantec.acqua.controller.ImpresionController
import com.sismantec.acqua.databinding.ActivityMenuConfiguracionBinding
import com.sismantec.acqua.Util.DownloadApk
import com.sismantec.acqua.Util.PeriodoPreferences
import com.sismantec.acqua.Util.SslNoSeguro
import com.sismantec.acqua.controller.ConexionController
import com.sismantec.acqua.controller.PeriodoController
import com.sismantec.acqua.database.LimpiarBD
import com.sismantec.acqua.factorie.ConfiguracionViewModelFactory
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.repository.BluetoothRepository
import com.sismantec.acqua.viewmodel.configViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MenuConfiguracion : AppCompatActivity() {

    private var versionAppServer : String? = null
    private var urlAppServer: String? = null
    private var versionActual : String = "1.10"
    private var conexionController = ConexionController()
    private var servidor: String = ""
    private var nombreServidor: String = ""
    private var utilidades = SslNoSeguro()
    private var isProcessing = false
    private var alerta : AlertDialogo? = null
    private var funciones = Funciones()
    private lateinit var appUpdate: TextView
    private lateinit var cancelUpdate: TextView
    private lateinit var binding: ActivityMenuConfiguracionBinding
    private val instancia = "CONFIG_SERVIDOR"
    private val instancia_periodo = "PERIODO_PREFACTURA"
    private lateinit var preferencias: SharedPreferences
    private lateinit var periodo_prefs: SharedPreferences
    private var ipServidor: String = ""
    private var puertoServidor: String = ""
    private var nombreImpresor: String = ""
    private var nombreEmpresa: String = ""
    private var idPeriodo: Int =0
    private var impressionController = ImpresionController(this@MenuConfiguracion)
    private var limpiarBD = LimpiarBD()
    private var periodoController = PeriodoController()
    private lateinit var confViewModel: configViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuracion ViewModel
        val bluetoothRepository = BluetoothRepository()
        val configFactory = ConfiguracionViewModelFactory(bluetoothRepository)
        confViewModel = ViewModelProvider(this,configFactory)[configViewModel::class.java]
        observarBluetoothViewModel()
        permisosBluetooth()

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        periodo_prefs = getSharedPreferences(instancia_periodo, Context.MODE_PRIVATE)
        ipServidor = preferencias.getString("ip", "").toString()
        puertoServidor = preferencias.getString("puerto", "").toString()
        nombreImpresor = preferencias.getString("impresorIntegrado", "noImpresor").toString()
        nombreEmpresa = preferencias.getString("dteNombreComercial", "").toString()
        idPeriodo = periodo_prefs.getInt("id_periodo",0)
        //Seteando el Puerto y la Ip del Servidor y el nombre comercial de la empresa
        binding.txtip.setText(ipServidor)
        binding.txtip.isEnabled = false
        binding.txtpuerto.setText(puertoServidor)
        binding.txtpuerto.isEnabled = false
        //binding.txtImpresor.setText(nombreImpresor)
        binding.texempresa.isEnabled = false
        binding.texempresa.setText(nombreEmpresa)
        alerta = AlertDialogo(this@MenuConfiguracion, this)

        //Ocultando Controles de Impresor
        if(preferencias.getString("tipoImpresora", "") == "BT"){
            binding.lyImpresor.visibility = View.GONE
        }

        //ACTIVANDO SWITCH DE IMRPESORES
        binding.swBluetooth.isChecked = preferencias.getString("tipoImpresora", "") == "BT"
        binding.swIntegrada.isChecked = preferencias.getString("tipoImpresora", "") == "INT"

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

        binding.btnUpdateApp.setOnClickListener {
            if (isProcessing) return@setOnClickListener
            deshabilitarOpcion()
            lifecycleScope.launch(Dispatchers.IO) {
                if (funciones.isInternetAvailable(this@MenuConfiguracion)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        obtenerNuevaVersionApp()
                    }//COURUTINA CARGAR DATOS DE ACTUALIZACION
                } else {
                    habilitarOpcion()
                    Toast.makeText(this@MenuConfiguracion, "ERROR AL VERIFICAR LA CONEXION A INTERNET", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.btnVerificarPeriodo.setOnClickListener {
            verificarPeriodo()
        }

        //VERSION APP
        versionActualApp()
        binding.lblVersionApp.text = "ACQUA APP Ver. $versionActual"

        onBackPressedDispatcher.addCallback(this) {}
    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            val intent = Intent(this@MenuConfiguracion, Inicio::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnConfigServidor.setOnClickListener {
            val intent = Intent(this@MenuConfiguracion, ConexionServidor::class.java)
            intent.putExtra("proviene", "actualizaConexion")
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

            val impresor = nombreImpresor

            if (impresor.isNotEmpty() && impresor != "-- NO SE ENCONTRARON IMPRESORAS --"){
                preferencias.edit{
                    remove("impresorIntegrado")
                    putString("impresorIntegrado", impresor)
                    Toast.makeText(this@MenuConfiguracion, "IMPRESOR CONFIGURADO", Toast.LENGTH_SHORT)
                        .show()
                }
            }else{
                Toast.makeText(this@MenuConfiguracion, "ERROR: SELECCIONE UNA IMPRESORA VÁLIDA", Toast.LENGTH_SHORT).show()
            }
        }

        binding.spImpresorasVinculadas.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                nombreImpresor = parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.imgLogoEmpresa.setOnClickListener {
            seleccionarImagen()
        }


    }

    //Funcion para los permisos Bluetooth
    private fun permisosBluetooth() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) !=
                PackageManager.PERMISSION_GRANTED){

                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }else{
                confViewModel.cargarImpresoras()
            }
        }else{
            confViewModel.cargarImpresoras()
        }

        /*
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
         */
    }
    private val requestBluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                confViewModel.cargarImpresoras()
            } else {
                Toast.makeText(this, "PERMISO DE BLUETOOTH DENEGADO", Toast.LENGTH_LONG).show()
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


    //FUNCIONALIDAD PARA DESCARGAR NUEVA VERSION DE LA APP

    //FUNCION PARA DESCARGAR Y EJECUTAR LA INSTALACION DE LA ACTUALIZACION
    private fun descargarVersionApp(url: String, filename: String){
        val downloadApk = DownloadApk(this@MenuConfiguracion)
        downloadApk.startDownloadingApk(url, filename);
    }


    //FUNCION PARA CREAR DIALOG ACTUALIZAR APP
    private fun mensajeUpdate(VersionServer: String, urlServer: String){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_update)
        appUpdate = updateDialog.findViewById(R.id.appUpdate)
        cancelUpdate = updateDialog.findViewById(R.id.cancelUpdate)

        appUpdate.setOnClickListener {
            updateDialog.dismiss()
            //----------------------------------
            //Condicion para reiniciar BD
            //----------------------------------
            Log.d("VERSION_ACTUAL_MSG_UPDATE","$versionActual")
            Log.d("VERSION_SERVER_MSG_UPDATE","$versionAppServer")
            if(versionActual.toDouble() < versionAppServer!!.toDouble()){
                lifecycleScope.launch(Dispatchers.IO) {

                    limpiarBD.limpiarBdAlActualizar(this@MenuConfiguracion)
                    withContext(Dispatchers.Main){
                        descargarVersionApp(urlServer, "UpdateApp_$VersionServer")
                    }
                }
            }else{
                descargarVersionApp(urlServer,"UpdateApp_$VersionServer")
            }
        }
        cancelUpdate.setOnClickListener {
            updateDialog.dismiss()
            habilitarOpcion()
            isProcessing = false
        }
        updateDialog.show()
    }
    private suspend fun ui(block: () -> Unit) =
        withContext(Dispatchers.Main) { block() }

    //FUNCION PARA ACTUALIZAR LA VERSION ACTUAL DE LA APP
    private fun versionActualApp(){
        val versionName= try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU){
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            }else{
                packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.versionName
        }catch (e: Exception){
            1f
        }
        versionActual = versionName.toString()
    }

    //FUNCION PARA VERIFICAR LA VERSION DE LA APP INSTALADA
    private suspend fun obtenerNuevaVersionApp() {
        try {
            val servidor = funciones.obtenerServidor(this@MenuConfiguracion)
            val direccion = servidor+"updateapp"
            val url = URL(direccion)
            Log.d("RUPDATE_DEBUG", "url: $url")
            //val sslContext = utilidades.crearSslInseguro()
            withContext(Dispatchers.Main) {
                alerta?.Cargando()
            }
            val connection = withContext(Dispatchers.IO) {
                (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 30000
                    readTimeout = 30000
                    requestMethod = "GET"
                    doInput = true
                    connect()
                }
            }
            if (connection.responseCode == 200){
                val response = withContext(Dispatchers.IO) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                }
                val respuesta = JSONArray(response)
                if (respuesta.length() >0){
                    for (i in 0 until respuesta.length()){
                        val dato = respuesta.getJSONObject(i)
                        versionAppServer = funciones.validateJsonIsnullString(dato, "version")
                        urlAppServer = funciones.validateJsonIsnullString(dato, "url")
                        runOnUiThread {
                            if(versionActual.toDouble() >= versionAppServer!!.toDouble()){
                                Log.d("VERSION_ACTUAL","$versionActual")
                                Log.d("VERSION_SERVER","$versionAppServer")
                                habilitarOpcion()
                                alerta?.dismisss()
                                Toast.makeText(applicationContext, "NO ES NECESARIO ACTUALIZAR", Toast.LENGTH_SHORT).show()
                            }else{
                                Log.d("VERSION_ACTUAL","$versionActual")
                                Log.d("VERSION_SERVER","$versionAppServer")
                                habilitarOpcion()
                                alerta?.dismisss()
                                mensajeUpdate(versionAppServer.toString(), urlAppServer.toString())
                            }
                        }
                    } // TERMINA EL FOR
                } else {
                    ui {
                        habilitarOpcion()
                        alerta?.dismisss()
                        Toast.makeText(applicationContext, "NO SE ENCONTRARON DATOS DE ACTUALIZACIOIN", Toast.LENGTH_SHORT).show()
                    }
                } // CASO QUE LA RESPUESTA VENGA VACIA
            }else {
                ui {
                    habilitarOpcion()
                    alerta?.dismisss()
                    ShowAlert("NO SE ENCONTRARON DATOS DE ACTUALIZACIOIN")
                }
            }
        }catch (e: Exception){
            ui {
                habilitarOpcion()
                alerta?.dismisss()
                ShowAlert("ERROR AL CONECTARSE CON EL SERVIDOR")
            }
            Log.e("UPDATE_APP", "Error: ${e.message}", e)
        }
    }

    private fun deshabilitarOpcion(){
        isProcessing = true

        binding.apply {
            btnImpresor.isEnabled = false
            btnUpdateApp.isEnabled = false
        }
    }
    private fun habilitarOpcion(){
        isProcessing = false

        binding.apply {
            btnImpresor.isEnabled = true
            btnUpdateApp.isEnabled = true
        }
    }

    private fun verificarPeriodo(){
        alerta?.Cargando()
        if (isProcessing) return

        isProcessing = true
        deshabilitarOpcion()

        lifecycleScope.launch {
            delay (300)
            runOnUiThread {
                alerta?.changeText("VERIFICANDO PERIODO")
            }

            val periodo = periodoController.verificarPeriodo(idPeriodo,this@MenuConfiguracion)
            if (!periodo){
                val periodoPrefs = PeriodoPreferences(this@MenuConfiguracion)
                periodoPrefs.limpiarPeriodo()
                Toast.makeText(this@MenuConfiguracion, "EL PERIODO YA NO ESTÁ ACTIVO", Toast.LENGTH_LONG).show()
                isProcessing = false
                habilitarOpcion()
                startActivity(
                    Intent(
                        this@MenuConfiguracion, Periodo::class.java))
                finish()
            }else{
                isProcessing = false
                habilitarOpcion()
                Toast.makeText(this@MenuConfiguracion, "EL PERIODO SE ENCUENTRA ACTIVO", Toast.LENGTH_LONG).show()
            }
            runOnUiThread {
                alerta?.dismisss()
            }
        }
    }

    private fun observarBluetoothViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                confViewModel.listaImpresoras.collect { lista ->
                    actualizarSpinnerImpresoras(lista)
                }
            }
        }
    }

    private fun actualizarSpinnerImpresoras(lista: List<String>){
        val displayList = if (lista.isEmpty()) {
            listOf("-- NO SE ENCONTRARON IMPRESORAS --")
        } else {
            lista
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spImpresorasVinculadas.adapter = adapter

        // Autoseleccionar la impresora guardada en preferencias
        val guardada = preferencias!!.getString("impresorIntegrado", "")
        if (!guardada.isNullOrEmpty() && lista.contains(guardada)) {
            val position = lista.indexOf(guardada)
            binding.spImpresorasVinculadas.setSelection(position)
        }

    }

    private fun ShowAlert(mensaje: String) {
        val alert: Snackbar = Snackbar.make(binding.vistaalerta, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(this@MenuConfiguracion, R.color.moderado))
        alert.show()
    }

    private fun ShowAlertPeriodo(mensaje: String){
        val alert: Snackbar = Snackbar.make(binding.vistaalertaperiodo, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(this@MenuConfiguracion, R.color.moderado))
        alert.show()
    }


}