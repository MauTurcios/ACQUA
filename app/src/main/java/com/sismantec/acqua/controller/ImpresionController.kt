package com.sismantec.acqua.controller

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.print.PrintManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.graphics.scale
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.usb.UsbConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.sismantec.acqua.Login
import com.sismantec.acqua.Periodo
import com.sismantec.acqua.R
import com.sismantec.acqua.Util.PeriodoPreferences
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.models.ConsumoResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.Locale


class ImpresionController(private val context: Context) {

    private lateinit var preferencias: SharedPreferences
    private lateinit var periodo_prefs: PeriodoPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var fhformato = Funciones().obtenerFechaHoraFormateada()
    //val config = Operativo(context)
    private val mutexImpresion = Mutex()


    //FUNCION PARA DETERMINAR LA CONEXION DE LA IMPRESORA
    fun imprimirRecibo(context: Context, datos: ConsumoResponse, onFinalizado: () -> Unit={}) {
        try {
            preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
            val tipoImpresora = preferencias.getString("tipoImpresora", "")
            when(tipoImpresora){
                "BT" -> {
                    if (!tienePermisoBt()){
                        Toast.makeText(context,"Permiso Bluetooth no concedido", Toast.LENGTH_LONG).show()
                        return
                    }
                    // ===============================
                    // Si no hay USB, probar Bluetooth
                    // ===============================
                    CoroutineScope(Dispatchers.IO).launch {
                        mutexImpresion.withLock {
                            try {
                                val btConnection = BluetoothPrintersConnections.selectFirstPaired()
                                if (btConnection != null){
                                    imprimirTicket(btConnection,context,datos)
                                }else{
                                    withContext(Dispatchers.Main){
                                        Toast.makeText(context,"No se encontró impresora bluetooth",Toast.LENGTH_LONG).show()
                                    }
                                }
                            }catch (e: Exception){
                                Log.e("IMPRESION_CONTROLLER_BT","[IMPRESION_CONTROLLER_BT] Error al imprimir",e)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Error al imprimir: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }finally {
                                withContext(Dispatchers.Main){
                                    onFinalizado()
                                }
                            }
                        }
                    }
                }
                "INT" ->{
                    if (!tienePermisoBt()){
                        Toast.makeText(context,"Permiso Bluetooth no concedido", Toast.LENGTH_LONG).show()
                        onFinalizado()
                        return
                    }
                    // ===============================
                    // Detectar impresora Integrada
                    // ===============================
                    try {
                        imprimirReciboIntegrado(context,datos)
                    }catch (e: Exception){
                        Log.e("IMPRESION_CONTROLLER","Error al imprimir",e)
                        Toast.makeText(context,"Error al imprimir: ${e.message}", Toast.LENGTH_LONG).show()
                    }finally {
                        onFinalizado()
                    }

                }
                else -> {
                    Toast.makeText(context,"No se ha configurado una impresora", Toast.LENGTH_LONG).show()
                    onFinalizado()
                }
            }
        } catch (e: Exception) {
            Log.e("IMPRESION_CONTROLLER","Error al imprimir: $e",e)
            Toast.makeText(context, "Error al imprimir: ${e.message}", Toast.LENGTH_LONG).show()
            onFinalizado()
        }
    }

    //FUNCION DEL FORMATO DEL TICKET
    private suspend fun imprimirTicket(connection: Any, context: Context, datos: ConsumoResponse) {
        preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        periodo_prefs = PeriodoPreferences(context)
        var vendedor : String = preferencias.getString("nombreEmpleado", "").toString()
        var direccion : String = preferencias.getString("dteDireccion","").toString()
        var empresa : String = preferencias.getString("dteNombreComercial","").toString()
        var giro : String = preferencias.getString("dteGiro","").toString()
        var nrc : String = preferencias.getString("dteNrc","").toString()
        var nit : String = preferencias.getString("dteNit","").toString()
        var fecha_incio = periodo_prefs.getPeriodoInicio()
        var fecha_fin = periodo_prefs.getPeriodoFin()
        var fecha_vencimiento = periodo_prefs.getPeriodoVencimiento()

        val textoPie = "ESTE DOCUMENTO NO TIENE VALIDEZ FISCAL"
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val printer = when(connection) {
            is UsbDevice -> EscPosPrinter(UsbConnection(usbManager, connection), 160, 48f, 32)
            is BluetoothConnection -> EscPosPrinter(connection, 160, 48f, 28)
            else -> null
        } ?: return

        val direccionFormateada = dividirEnLineas(direccion, 31)
        val empresaFormateada = dividirEnLineas(empresa, 31)
        val giroFormateada = dividirEnLineas(giro, 31)
        val textoPieFormateado = dividirEnLineas(textoPie, 31)
        val nrcFormateado = dividirEnLineas(nrc,31)
        val nitFormateado = dividirEnLineas(nit, 31)
        val giroCliente = dividirEnLineas("", 31)
        val direccionCliente = dividirEnLineas(datos.direccion, 31)
        // ===============================
        // Preparar logo y texto
        // ===============================
        val prefs = context.getSharedPreferences("MisImagenes", MODE_PRIVATE)
        val filePath = prefs.getString("imagenFile", null)

        // Variable para el logo final
        val logoOriginal: Bitmap = if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.nologo)
            }
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.nologo)
        }

        // Redimensionar
        val logoRedimensionado = redimensionarLogo(logoOriginal, 384)

        // ===============================
        // Construir ticket Normal
        // ===============================
        val ticket = StringBuilder()
            .append("[C]<img>")
            .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado))
            .append("</img>\n")
            .append("[C]AVISO DE COBRO\n\n")
            .append("LECTURA MICROMEDIDOR\n")
            .append("[C]$empresaFormateada\n")
            .append("[C]$direccionFormateada\n")
            .append("[C]$nrcFormateado\n")
            .append("[C]$nitFormateado\n")
            .append("[C]$giroFormateada\n")
            .append("[L]-------------------------------\n")
            .append("[C]PERIODO DEL: $fecha_incio \n")
            .append("[C]AL: $fecha_fin \n")
            .append("[L]-------------------------------\n")
            .append("[C]CARGO FIJO\n")
            .append(String.format("[L]%-23s%8s\n", "DESCRIPCION", "VALOR"))
            .append("[L]-------------------------------\n")
            .append(construirCargoFijo(datos))
            .append("[L]-------------------------------\n\n")
            .append("[C]CARGO POR CONSUMO\n")
            .append(String.format("[L]%-12s%9s%9s\n", "DESCRIPCION", "VALOR", "ALCANT."))
            .append("[L]-------------------------------\n")
            .append(filaTablaTarifario("0  A ${datos.primeros_m3.toInt()}M3", "$${datos.primeros_m3_valor}", "$ 2.00"))
            .append(filaTablaTarifario("${datos.e1_minimo_m3.toInt()} A ${datos.e1_maximo_m3.toInt()}M3", "$${datos.e1_valor_m3}", "$ 2.00"))
            .append(filaTablaTarifario("${datos.e2_minimo_m3.toInt()} A ${datos.e2_maximo_m3.toInt()}M3", "$${datos.e2_valor_m3}", "$ 2.00"))
            .append(filaTablaTarifario("${datos.e3_minimo_m3.toInt()} A ${datos.e3_maximo_m3.toInt()}M3", "$${datos.e3_valor_m3}", "$ 2.00"))
            .append(filaTablaTarifario("${datos.e4_minimo_m3.toInt()} A ${datos.e4_maximo_m3.toInt()}M3", "$${datos.e4_valor_m3}", "$ 3.50"))
            .append(filaTablaTarifario("${datos.e5_minimo_m3.toInt()} A ${datos.e5_maximo_m3.toInt()}M3", "$${datos.e5_valor_m3}", "$ 3.50"))
            .append(filaTablaTarifario("${datos.e6_minimo_m3.toInt()} A ${datos.e6_maximo_m3.toInt()}M3", "$${datos.e6_valor_m3}", "$ 3.50"))
            .append(filaTablaTarifario("${datos.e7_minimo_m3.toInt()} A ${datos.e7_maximo_m3.toInt()}M3", "$${datos.e7_valor_m3}", "$ 3.50"))
            .append(filaTablaTarifario("${datos.e8_minimo_m3.toInt()} A MAS", " ${datos.e8_valor_m3}", "$ 3.50"))
            .append("[L]-------------------------------\n")
            .append("[C]DATOS DEL CLIENTE\n")
            .append("[L]-------------------------------\n")
            .append("[C]FECHA: ${fhformato} \n")
            .append("[L]NUM DE CUENTA: <u><font size='big'>${datos.cuenta}</font></u>\n")
            .append("[L]NOMBRE:\n")
            .append("[C]${datos.nombre}\n")
            .append("[L]DOCUMENTO: ${datos.documento} \n")
            .append("[L]DIRECCION: \n")
            .append("[L]$direccionCliente\n")
            .append("[L]-------------------------------\n")
            .append("[C]DETALLE DEL DOCUMENTO\n")
            .append("[L]-------------------------------\n")
            .append(String.format("[L]%-9s%9s\n", "L.ACT.", "L.ANTE."))
            .append(filaTablaLecturas1("${datos.lecturaActual}M3", "${datos.lecturaAnterior}M3"))
            .append("[L]-------------------------------\n")
            .append(String.format("[L]%-9s%9s\n", "CONS. M3.", "VALOR"))
            .append(filaTablaLecturas2("${datos.consumo}M3", "   $ ${String.format("%.2f", 0.72)}"))
            .append("[L]-------------------------------\n")
            .append(String.format("[L]%-12s%3s%6s%6S\n", "", "", "  PRECIO", ""))
            .append(String.format("[L]%-12s%3s%6s%6S\n", "  DESCRIP", "CANT", "UNI.", "  TOTAL"))
            .append("[L]-------------------------------\n")
            .append(filaTablaTotales("COST. ADMIN", "1", "$ ${String.format("%.2f", 3.58)}", "$ ${String.format("%.2f", 3.58)}"))
            .append(filaTablaTotales("MANTTO. SIST. AGUA POTABLE", "1", "$ ${String.format("%.2f", 4.42)}", "$ ${String.format("%.2f", 4.42)}"))
            .append(filaTablaTotales("CONSU. AGUA M3", "3", "$ ${String.format("%.2f", 0.24)}", "$ ${String.format("%.2f", 0.72)}"))
            .append(filaTablaTotales("CANON POR M3", "3", "$ ${String.format("%.2f", 0.07)}", "$ ${String.format("%.2f", 0.21)}"))
            .append(filaTablaTotales("MANTTO ALCANT. SANITARIO", "1", "$ ${String.format("%.2f", 2.00)}", "$ ${String.format("%.2f", 2.00)}"))
            .append(filaTablaTotales("PAGO DE NOVIEMBRE", "1", "\$ ${String.format("%.2f", 0.00)}", "$ ${String.format("%.2f", 0.00)}"))
            .append("[L]-------------------------------\n")
            .append(filaTablaTotales("TOTAL", "", "", "$ ${String.format("%.2f", 10.93)}"))
            .append("[L]-------------------------------\n\n")
            .append("[L]ENTREGADO POR: ${vendedor}\n")
            .append("[C]<b>$textoPieFormateado</b>\n")
            .append(" \n")


        /*
        *   .append("[L]LECTURA ACTUAL: [R] 35M3 \n")
            .append("[L]LECTURA ANTERIOR: [R] 24M3 \n")
            .append("[L]CONSUMO M3: [R] 11M3 \n")
            .append("[L]VALOR: [R] $ ${String.format("%.2f", 25.50)} \n")
        * */


        val textoImprmir = normalizarTexto(ticket.toString())
        printer.printFormattedText(textoImprmir)
    }

    //FUNCION PARA IMPRIMIR EL RECIBO INTREGRADO

    @SuppressLint("MissingPermission")
    private fun imprimirReciboIntegrado(context: Context, datos: ConsumoResponse){

        // Verificación real antes de acceder a Bluetooth
        if (!tienePermisoBt()) {
            Toast.makeText(
                context,
                "Permiso Bluetooth no concedido",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        periodo_prefs = PeriodoPreferences(context)
        var vendedor : String = preferencias.getString("nombreEmpleado", "").toString()
        var direccion : String = preferencias.getString("dteDireccion","").toString()
        var empresa : String = preferencias.getString("dteNombreComercial","").toString()
        var giro : String = preferencias.getString("dteGiro","").toString()
        var nrc : String = preferencias.getString("dteNrc","").toString()
        var nit : String = preferencias.getString("dteNit","").toString()
        var fecha_incio = periodo_prefs.getPeriodoInicio()
        var fecha_fin = periodo_prefs.getPeriodoFin()
        var fecha_vencimiento = periodo_prefs.getPeriodoVencimiento()

        val textoPie = "ESTE DOCUMENTO NO TIENE VALIDEZ FISCAL"

        val impresorIntegrado = preferencias.getString("impresorIntegrado", "sinNombre")?:""

        if (impresorIntegrado.isBlank()) {
            Toast.makeText(
                context,
                "No hay impresora integrada configurada",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(
                context,
                "El dispositivo no dispone de Bluetooth",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val device = bluetoothAdapter.bondedDevices.firstOrNull(){ device ->
            Log.d(
                "IMPRESION_INT",
                "Vinculado -> Nombre: ${device.name}, MAC: ${device.address}"
            )
            device.name?.contains(
                impresorIntegrado,
                ignoreCase = true
            ) == true
        }

        if(device != null){
            val connection = BluetoothConnection(device)

            connection.connect()

            val printer = EscPosPrinter(connection, 160, 48f, 28)

            val direccionFormateada = dividirEnLineas(direccion, 31)
            val empresaFormateada = dividirEnLineas(empresa, 31)
            val giroFormateada = dividirEnLineas(giro, 31)
            val textoPieFormateado = dividirEnLineas(textoPie, 31)
            val nrcFormateado = dividirEnLineas(nrc,31)
            val nitFormateado = dividirEnLineas(nit, 31)
            val giroCliente = dividirEnLineas("", 31)
            val direccionCliente = dividirEnLineas(datos.direccion, 31)

            // ===============================
            // Preparar logo y texto
            // ===============================
            val prefs = context.getSharedPreferences("MisImagenes", MODE_PRIVATE)
            val filePath = prefs.getString("imagenFile", null)

            // Variable para el logo final
            val logoOriginal: Bitmap = if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    BitmapFactory.decodeResource(context.resources, R.drawable.nologo)
                }
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.nologo)
            }

            // Redimensionar
            val logoRedimensionado = redimensionarLogo(logoOriginal, 384)


            // ===============================
            // Construir ticket Normal
            // ===============================
            val ticket = StringBuilder()
                .append("[C]<img>")
                .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado))
                .append("</img>\n")
                .append("[C]AVISO DE COBRO\n\n")
                .append("LECTURA MICROMEDIDOR\n")
                .append("[C]$empresaFormateada\n")
                .append("[C]$direccionFormateada\n")
                .append("[C]$nrc\n")
                .append("[C]$nit\n")
                .append("[C]$giroFormateada\n")
                .append("[L]-------------------------------\n")
                .append("[C]PERIODO DEL: $fecha_incio \n")
                .append("[C]AL: $fecha_fin \n")
                .append("[L]-------------------------------\n")
                .append("[C]CARGO FIJO\n")
                .append(String.format("[L]%-23s%8s\n", "DESCRIPCION", "VALOR"))
                .append("[L]-------------------------------\n")
                .append(construirCargoFijo(datos))
                .append("[L]-------------------------------\n\n")
                .append("[C]CARGO POR CONSUMO\n")
                .append(String.format("[L]%-12s%9s%9s\n", "DESCRIPCION", "VALOR", "ALCANT."))
                .append("[L]-------------------------------\n")
                .append(filaTablaTarifario("0  A ${datos.primeros_m3.toInt()} M3", "$${datos.primeros_m3_valor}", "$ 2.00"))
                .append(filaTablaTarifario("${datos.e1_minimo_m3.toInt()} A ${datos.e1_maximo_m3.toInt()}M3", "$ ${datos.e1_valor_m3}", "$ 2.00"))
                .append(filaTablaTarifario("${datos.e2_minimo_m3.toInt()} A ${datos.e2_maximo_m3.toInt()}M3", "$ ${datos.e2_valor_m3}", "$ 2.00"))
                .append(filaTablaTarifario("${datos.e3_minimo_m3.toInt()} A ${datos.e3_maximo_m3.toInt()}M3", "$ ${datos.e3_valor_m3}", "$ 2.00"))
                .append(filaTablaTarifario("${datos.e4_minimo_m3.toInt()} A ${datos.e4_maximo_m3.toInt()}M3", "$ ${datos.e4_valor_m3}", "$ 3.50"))
                .append(filaTablaTarifario("${datos.e5_minimo_m3.toInt()} A ${datos.e5_maximo_m3.toInt()}M3", "$ ${datos.e5_valor_m3}", "$ 3.50"))
                .append(filaTablaTarifario("${datos.e6_minimo_m3.toInt()} A ${datos.e6_maximo_m3.toInt()}M3", "$ ${datos.e6_valor_m3}", "$ 3.50"))
                .append(filaTablaTarifario("${datos.e7_minimo_m3.toInt()} A ${datos.e7_maximo_m3.toInt()}M3", "$ ${datos.e7_valor_m3}", "$ 3.50"))
                .append(filaTablaTarifario("${datos.e8_minimo_m3.toInt()} A MAS", "$ ${datos.e8_valor_m3}", "$ 3.50"))
                .append("[L]-------------------------------\n")
                .append("[C]DATOS DEL CLIENTE\n")
                .append("[L]-------------------------------\n")
                .append("[C]FECHA: ${fhformato} \n")
                .append("[L]NUM DE CUENTA: <u><font size='big'>${datos.cuenta}</font></u>\n")
                .append("[L]NOMBRE:\n")
                .append("[C]${datos.nombre}\n")
                .append("[L]DOCUMENTO: ${datos.documento} \n")
                .append("[L]DIRECCION: \n")
                .append("[L]$direccionCliente\n")
                .append("[L]-------------------------------\n")
                .append("[C]DETALLE DEL DOCUMENTO\n")
                .append("[L]-------------------------------\n")
                .append(String.format("[L]%-12s%12s\n", "L.ACT.", "L.ANTE."))
                .append(filaTablaLecturas1("${datos.lecturaActual}M3", "${datos.lecturaAnterior}M3"))
                .append("[L]-------------------------------\n")
                .append(String.format("[L]%-12s%12s\n", "CONS. M3.", "VALOR"))
                .append(filaTablaLecturas2("${datos.consumo}M3", "   $ ${String.format("%.2f", 0.72)}"))
                .append("[L]-------------------------------\n")
                .append(String.format("[L]%-12s%3s%6s%6S\n", "", "", "  PRECIO", ""))
                .append(String.format("[L]%-12s%3s%6s%6S\n", "  DESCRIP", "CANT", "UNI.", "  TOTAL"))
                .append("[L]-------------------------------\n")
                .append(filaTablaTotales("COST. ADMIN", "1", "$ ${String.format("%.2f", 3.58)}", "$ ${String.format("%.2f", 3.58)}"))
                .append(filaTablaTotales("MANTTO. SIST. AGUA POTABLE", "1", "$ ${String.format("%.2f", 4.42)}", "$ ${String.format("%.2f", 4.42)}"))
                .append(filaTablaTotales("CONSU. AGUA M3", "3", "$ ${String.format("%.2f", 0.24)}", "$ ${String.format("%.2f", 0.72)}"))
                .append(filaTablaTotales("CANON POR M3", "3", "$ ${String.format("%.2f", 0.07)}", "$ ${String.format("%.2f", 0.21)}"))
                .append(filaTablaTotales("MANTTO ALCANT. SANITARIO", "1", "$ ${String.format("%.2f", 2.00)}", "$ ${String.format("%.2f", 2.00)}"))
                .append(filaTablaTotales("PAGO DE NOVIEMBRE", "1", "\$ ${String.format("%.2f", 0.00)}", "$ ${String.format("%.2f", 0.00)}"))
                .append("[L]-------------------------------\n")
                .append(filaTablaTotales("TOTAL", "", "", "$ ${String.format("%.2f", 10.93)}"))
                .append("[L]-------------------------------\n\n")
                .append("[L]ENTREGADO POR: ${vendedor}\n")
                .append("[C]<b>$textoPieFormateado</b>\n")
                .append(" \n")

            val textoImprmir = normalizarTexto(ticket.toString())
            printer.printFormattedText(textoImprmir)

        }else{
            Toast.makeText(context, "NO ENCONTRADO", Toast.LENGTH_SHORT)
                .show()
        }

    }

    //Funcion para simular una tabla
    private fun filaTablaTarifario(desc: String, valor: String, alcan: String): String {
        return String.format(
            "[L]%-12s%9s%9s\n",
            desc.take(12),
            valor.take(9),
            alcan.take(9)
        )
    }

    //Funcion para simular una tabla
    private fun filaTablafIJO(desc: String, valor: String): String {
        return String.format(
            "[L]%-23s%8s\n",
            desc.take(23),
            valor.take(8)
        )
    }

    //Funcion para simular una tabla
    private fun filaTablaTotales(desc: String, cant: String, preciou: String, total:String): String {
        return String.format(
            "[L]%-12s%3s%8s%8s\n",
            desc.take(12),
            cant.take(3),
            preciou.take(8),
            total.take(8)
        )
    }

    private fun filaTablaLecturas1(lActual: String, lAnterior: String): String {
        return String.format(
            "[L]%-7s%12s\n",
            lActual.take(12),
            lAnterior.take(12)
        )
    }
    private fun filaTablaLecturas2(consumo: String, total:String): String {
        return String.format(
            "[L]%-7s%12s\n",
            consumo.take(12),
            total.take(12)
        )
    }

    //Funcion para dividir en lineas
    private fun dividirEnLineas(texto: String, maxCaracteres: Int): String {
        return texto.chunked(maxCaracteres).joinToString("\n[C]")
    }

    private fun normalizarTexto(texto: String): String {
        val original = "ÁÀÂÄáàâäÉÈÊËéèêëÍÌÎÏíìîïÓÒÔÖóòôöÚÙÛÜúùûüÑñÇç"
        val reemplazo = "AAAAaaaaEEEEeeeeIIIIiiiiOOOOooooUUUUuuuuNnCc"

        var resultado = texto
        for (i in original.indices) {
            resultado = resultado.replace(original[i], reemplazo[i])
        }

        // Elimina caracteres no ASCII
        resultado = resultado.replace(Regex("[^\\x00-\\x7F]"), "")
        return resultado
    }

    //Funcion para redimencionar el logo
    private fun redimensionarLogo(bitmap: Bitmap, anchoMaximo: Int) : Bitmap {
        val proporcion = anchoMaximo.toFloat() / bitmap.width
        val altoNuevo = (bitmap.height * proporcion).toInt()

        return bitmap.scale(anchoMaximo, altoNuevo)
    }


    //VERIFICAR PERMISOS BLUETOOTH
    private fun tienePermisoBt(): Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        )== PackageManager.PERMISSION_GRANTED
    }

    private data class CargoFijo(
        val linea: String,
        val descripcion: String,
        val precio: Double
    )

    private fun construirCargoFijo(datos: ConsumoResponse): String{
        val cargos = listOf(
            CargoFijo(datos.cf1_linea, datos.cf1_descripcion,datos.cf1_precio),
            CargoFijo(datos.cf2_linea, datos.cf2_descripcion,datos.cf2_precio),
            CargoFijo(datos.cf3_linea, datos.cf3_descripcion,datos.cf3_precio),
            CargoFijo(datos.cf4_linea, datos.cf4_descripcion,datos.cf4_precio),
            CargoFijo(datos.cf5_linea, datos.cf5_descripcion,datos.cf5_precio)
        ).filter { it.descripcion.isNotBlank() }
        return buildString {
            cargos.forEach {
                cargo ->
                if (cargo.linea.isNotBlank()){
                    val linea = cargo.linea.trim().let {
                        if (it.endsWith(":")) it else "$it:"
                    }
                    append(filaTablafIJO(linea,""))
                }
                append(
                    filaTablafIJO(
                        "- ${cargo.descripcion.trim()}",
                        formatearMoneda(cargo.precio)
                    )
                )
            }
            append(
                filaTablafIJO(
                    "TOTAL",
                    formatearMoneda(cargos.sumOf { it.precio })
                )
            )
        }
    }

    private fun formatearMoneda(valor: Double): String {
        return String.format(Locale.US, "\$%.2f", valor)
    }

}