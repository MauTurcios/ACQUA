package com.sismantec.acqua.funciones

import android.Manifest
import android.animation.Animator
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.view.ViewAnimationUtils
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sismantec.acqua.database.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AlertDialog
import com.sismantec.acqua.AlertDialogo
import com.sismantec.acqua.R

class Funciones {
    private lateinit var preferences : SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var ip : String = ""
    private var puerto: String = ""
    private var alert: AlertDialogo? = null

    //----------------
    //Funcion para Obtener IP del Servidor al conectar
    //----------------
    fun servidor(ip: String?, puerto: String?) : String{
        return "http://${ip}:${puerto}/api/"
    }

    //------------------------
    //Funcion para obtener el server
    //------------------------
    fun obtenerServidor(context : Context) : String{

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        preferences.apply {
            ip = getString("ip", "").toString()
            puerto  = getString("puerto", "").toString()
        }

        val servidor = servidor(ip, puerto)

        return servidor
    }

    //----------------
    //Funcion para obtener la instancia de BD
    //----------------
    fun obtenerInstancia(context: Context) : AppDataBase {
        val base = AppDataBase.obtenerInstancia(context)
        return base
    }

    //----------------
    //FUNCION PARA VERIFICAR LA CONEXION A INTERNET
    //----------------
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET])
    suspend fun isInternetAvailable(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            if (isNetworkAvailable(context)) {
                try {
                    val url = URL("https://clients3.google.com/generate_204")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 1500
                    connection.readTimeout = 1500
                    connection.requestMethod = "GET"
                    connection.connect()
                    connection.responseCode == 204
                } catch (e: IOException) {
                    false
                }
            } else false
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    //------------------
    //FUNCION PARA OBTENER LA FECHA FORMATEADA
    //------------------
    fun obtenerFechaFormateada() : String{
        val fecha = LocalDate.now()
        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val fechaFormateada = fecha.format(formato)

        return fechaFormateada
    }

    //--------------------
    //FUNCION PARA OBTENER LA FECHA Y HORA FORMATEADA
    //--------------------
    fun obtenerFechaHoraFormateada() : String{
        val fechaFormato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val fecha = fechaFormato.format(Date())

        return fecha
    }
    //FUNCIONES DE VALIDACION DEL WS
    fun validate(parametro: String?): String {
        if (parametro != null && parametro.length > 0) {
            return parametro.trim()
        } else {
            return ""
        }
    }

    //FUNCION DE MENSAJE DE ERROR
    fun mostrarAlerta(mensaje: String, context: Context, view: View){
        val alert: Snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_light_error))
        alert.show()
    }

    //FUNCION DE MENSAJE OK
    fun mostrarMensaje(mensaje: String, context: Context, view: View){
        val alert: Snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG)
        alert.view.setBackgroundColor(ContextCompat.getColor(context, R.color.btnAddColor))
        alert.show()
    }

    //MENSANJE ASINCRONO
    fun messageAsync(mensaje: String) {
        if (alert != null) {
            alert!!.changeText(mensaje)
        }
    }

    fun validate(parametro: Int?): Int {
        return parametro ?: 0
    }

    fun validate(parametro: Float?): Float {
        return parametro ?: 0.00.toFloat()
    }

    fun validateJsonIsNullInt(json: JSONObject, campo: String): Int {
        return if (json.isNull(campo)) {
            0
        } else {
            json.getInt(campo)
        }
    }

    fun validateJsonIsNullFloat(json: JSONObject, campo: String): Float {
        return if (json.isNull(campo)) {
            0.toFloat()
        } else {
            json.getString(campo).toFloat()
        }
    }

    fun validateJsonIsnullString(json: JSONObject, campo: String): String {
        return if (json.isNull(campo)) {
            ""
        } else {
            json.getString(campo).trim()
        }
    }

    fun validateJsonDate(json: JSONObject, campo: String): String {
        val fechaPorDefecto = "1900-01-01T00:00:00"
        return if (json.isNull(campo) || json.getString(campo).trim().isEmpty()) {
            fechaPorDefecto
        } else {
            try {
                val input = json.getString(campo).trim()
                val fecha = LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            } catch (e: Exception) {
                fechaPorDefecto
            }
        }
    }
    //FIN DE FUNCIONES DE VALIDACION

    /**
     * FUNCION DE ANIMACION CIRCULAR PARA LOS RECYCLER VIEW
     */
    fun AnimacionCircularReavel(view: View) {
        val centerx = 0
        val centery = 0
        val starRadius = 0.00
        val endRadius = Math.max(view.width, view.height)
        val animacion: Animator? =
            ViewAnimationUtils.createCircularReveal(
                view, centerx, centery, starRadius.toFloat(),
                endRadius.toFloat()
            )
        view.visibility = View.VISIBLE
        animacion!!.start()

    }
}