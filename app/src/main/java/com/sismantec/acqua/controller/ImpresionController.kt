package com.sismantec.acqua.controller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.print.PrintManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.graphics.scale
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.usb.UsbConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.sismantec.acqua.R
import com.sismantec.acqua.funciones.Operativo
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.entities.ConfigEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class ImpresionController(private val context: Context) {

    private lateinit var preferencias: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    val config = Operativo(context)
    //FUNCION PARA DETERMINAR LA CONEXION DE LA IMPRESORA
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun imprimirRecibo(context: Context) {
        preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        try {
            val tipoImpresora = preferencias.getString("tipoImpresora", "")
            when(tipoImpresora){
                "BT" -> {
                    // ===============================
                    // Si no hay USB, probar Bluetooth
                    // ===============================
                    val btConnection = BluetoothPrintersConnections.selectFirstPaired()
                    if (btConnection != null) {
                        //imprimirTicket(btConnection, context)
                        CoroutineScope(Dispatchers.IO).launch {
                            if (config == null) {
                                config.cargarConfig()
                            }
                            imprimirTicket(btConnection, context, config)
                        }
                    } else {
                        Toast.makeText(context, "No se encontró impresora USB ni Bluetooth", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    // ===============================
                    // Detectar impresora Integrada
                    // ===============================
                    imprimirReciboIntegrado(context,config)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al imprimir: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    //FUNCION DEL FORMATO DEL TICKET
    private suspend fun imprimirTicket(connection: Any, context: Context, config: Operativo) {
        val textoPie = "ESTE DOCUMENTO NO TIENE VALIDEZ FISCAL"
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val printer = when(connection) {
            is UsbDevice -> EscPosPrinter(UsbConnection(usbManager, connection), 160, 48f, 32)
            is BluetoothConnection -> EscPosPrinter(connection, 160, 48f, 28)
            else -> null
        } ?: return

        val direccionFormateada = dividirEnLineas(config.direccion(), 32)
        val empresaFormateada = dividirEnLineas(config.empresa(), 32)
        val giroFormateada = dividirEnLineas(config.giro(), 32)
        val textoPieFormateado = dividirEnLineas(textoPie, 32)
        val nrc = dividirEnLineas(config.nrc(),32)
        val nit = dividirEnLineas(config.nit(), 32)
        val giroCliente = dividirEnLineas("", 32)
        val direccionCliente = dividirEnLineas("COLONIA LA PRADERA, POLIGONO D-05 CASA 26, SAN MIGUEL,SAN MIGUEL",32)

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
            .append("[L]--------------------------------\n")
            .append("[C]PERIODO DEL 2026-02-19 \n")
            .append("[C]AL 2026-03-19 \n")
            .append("[L]--------------------------------\n")
            .append("[C]CARGO FIJO\n")
            .append(String.format("[L]%-23s%9s\n", "DESCRIPCION", "VALOR"))
            .append("[L]--------------------------------\n")
            .append(filaTablafIJO("Administracion: ",""))
            .append(filaTablafIJO(" - Cost. Administrativos ","$3.58"))
            .append(filaTablafIJO("Mantenimiento: ",""))
            .append(filaTablafIJO(" - Sist. Agua Potable","$4.42"))
            .append(filaTablafIJO(" - Alcant. Sanitario","$2.00"))
            .append(String.format("[L]%-23s%9s\n", "TOTAL", "$10.00"))
            .append("[L]--------------------------------\n\n")
            .append("[C]CARGO POR CONSUMO\n")
            .append(String.format("[L]%-12s%9s%9s\n", "DESCRIPCION", "VALOR", "ALCANT."))
            .append("[L]--------------------------------\n")
            .append(filaTablaTarifario("0  A 15M3", "$ 0.24", "$ 2.00"))
            .append(filaTablaTarifario("16 A 25M3", "$ 0.36", "$ 2.00"))
            .append(filaTablaTarifario("26 A 35M3", "$ 0.48", "$ 2.00"))
            .append(filaTablaTarifario("36 A 45M3", "$ 0.66", "$ 2.00"))
            .append(filaTablaTarifario("45 A 60M3", "$ 0.78", "$ 3.50"))
            .append(filaTablaTarifario("61 A 70M3", "$ 0.84", "$ 3.50"))
            .append(filaTablaTarifario("71 A 80M3", "$ 0.96", "$ 3.50"))
            .append(filaTablaTarifario("81 A 90M3", "$ 1.20", "$ 3.50"))
            .append(filaTablaTarifario("91 A MAS", " $ 1.44", "$ 3.50"))
            .append("[L]--------------------------------\n")
            .append("[C]DATOS DEL CLIENTE\n")
            .append("[L]--------------------------------\n")
            .append("[L]NUM DE CUENTA: <u><font size='big'>EE1286</font></u>\n")
            .append("[L]NOMBRE:\n")
            .append("[C]SALVADOR GARCIA FUENTES\n")
            .append("[L]DOCUMENTO: 123456789 \n")
            .append("[L]DIRECCION: \n")
            .append("[L]$direccionCliente\n")
            .append("[L]--------------------------------\n")
            .append("[C]DETALLE DEL DOCUMENTO\n")
            .append("[L]--------------------------------\n")
            .append(String.format("[L]%-7s%7s%7s%7S\n", "L.ACT.", "L.ANTE.", " CONS. M3.", "VALOR"))
            .append(filaTablaLecturas("35M3", "24M3", "11M3", "   $ ${String.format("%.2f", 0.72)}"))
            .append("[L]--------------------------------\n")
            .append(String.format("[L]%-12s%3s%6s%6S\n", "", "", "  PRECIO", ""))
            .append(String.format("[L]%-12s%3s%6s%6S\n", "  DESCRIP", "CANT", "UNI.", "  TOTAL"))
            .append("[L]--------------------------------\n")
            .append(filaTablaTotales("COST. ADMIN", "1", "$ ${String.format("%.2f", 3.58)}", "$ ${String.format("%.2f", 3.58)}"))
            .append(filaTablaTotales("MANTTO. SIST. AGUA POTABLE", "1", "$ ${String.format("%.2f", 4.42)}", "$ ${String.format("%.2f", 4.42)}"))
            .append(filaTablaTotales("CONSU. AGUA M3", "3", "$ ${String.format("%.2f", 0.24)}", "$ ${String.format("%.2f", 0.72)}"))
            .append(filaTablaTotales("CANON POR M3", "3", "$ ${String.format("%.2f", 0.07)}", "$ ${String.format("%.2f", 0.21)}"))
            .append(filaTablaTotales("MANTTO ALCANT. SANITARIO", "1", "$ ${String.format("%.2f", 2.00)}", "$ ${String.format("%.2f", 2.00)}"))
            .append(filaTablaTotales("PAGO DE NOVIEMBRE", "1", "\$ ${String.format("%.2f", 0.00)}", "$ ${String.format("%.2f", 0.00)}"))
            .append("[L]--------------------------------\n")
            .append(filaTablaTotales("TOTAL", "", "", "$ ${String.format("%.2f", 10.93)}"))
            .append("[L]--------------------------------\n\n")
            .append("[L]ENTREGADO POR: ANTONIO HERNANDEZ\n")
            .append("[C]FECHA: 02-01-2026 \n")
            .append("[C]<b>$textoPieFormateado</b>\n")
            .append(" \n")


        /*
        *   .append("[L]LECTURA ACTUAL: [R] 35M3 \n")
            .append("[L]LECTURA ANTERIOR: [R] 24M3 \n")
            .append("[L]CONSUMO M3: [R] 11M3 \n")
            .append("[L]VALOR: [R] $ ${String.format("%.2f", 25.50)} \n")
        * */


        val textoImprmir = normalizarTexto(ticket.toString())
    }

    //FUNCION PARA IMPRIMIR EL RECIBO INTREGRADO
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun imprimirReciboIntegrado(context: Context, config: Operativo){
        val textoPie = "ESTE DOCUMENTO NO TIENE VALIDEZ FISCAL"

        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val impresorIntegrado = preferencias.getString("impresorIntegrado", "sinNombre")

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device : BluetoothDevice? = bluetoothAdapter.bondedDevices.firstOrNull {
            it.name.contains(impresorIntegrado.toString())
        }

        if(device != null){
            val connection = BluetoothConnection(device)

            connection.connect()

            val printer = EscPosPrinter(connection, 160, 48f, 28)

            val direccionFormateada = dividirEnLineas(config.direccion(), 32)
            val empresaFormateada = dividirEnLineas(config.empresa(), 32)
            val giroFormateada = dividirEnLineas(config.giro(), 32)
            val nrc = dividirEnLineas(config.nrc(),32)
            val nit = dividirEnLineas(config.nit(), 32)
            val textoPieFormateado = dividirEnLineas(textoPie, 32)
            val giroCliente = dividirEnLineas("", 32)
            val direccionCliente = dividirEnLineas("RESIDENCIAL LA PRADERA, POLIGONO D-05 CASA 26, SAN MIGUEL,SAN MIGUEL",32)

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
                .append("[L]--------------------------------\n")
                .append("[C]PERIODO DEL 2025-12-19 \n")
                .append("[C]AL 2026-01-19 \n")
                .append("[L]--------------------------------\n")
                .append("[C]CARGO FIJO\n")
                .append(String.format("[L]%-23s%9s\n", "DESCRIPCION", "VALOR"))
                .append("[L]--------------------------------\n")
                .append(filaTablafIJO("Administracion: ",""))
                .append(filaTablafIJO(" - Cost. Administrativos ","$3.58"))
                .append(filaTablafIJO("Mantenimiento: ",""))
                .append(filaTablafIJO(" - Sist. Agua Potable","$4.42"))
                .append(filaTablafIJO(" - Alcant. Sanitario","$2.00"))
                .append(String.format("[L]%-23s%9s\n", "TOTAL", "$10.00"))
                .append("[L]--------------------------------\n\n")
                .append("[C]CARGO POR CONSUMO\n")
                .append(String.format("[L]%-12s%9s%9s\n", "DESCRIPCION", "VALOR", "ALCANT."))
                .append("[L]--------------------------------\n")
                .append(filaTablaTarifario("0  A 15M3", "$ 0.24", "$ 2.00"))
                .append(filaTablaTarifario("16 A 25M3", "$ 0.36", "$ 2.00"))
                .append(filaTablaTarifario("26 A 35M3", "$ 0.48", "$ 2.00"))
                .append(filaTablaTarifario("36 A 45M3", "$ 0.66", "$ 2.00"))
                .append(filaTablaTarifario("45 A 60M3", "$ 0.78", "$ 3.50"))
                .append(filaTablaTarifario("61 A 70M3", "$ 0.84", "$ 3.50"))
                .append(filaTablaTarifario("71 A 80M3", "$ 0.96", "$ 3.50"))
                .append(filaTablaTarifario("81 A 90M3", "$ 1.20", "$ 3.50"))
                .append(filaTablaTarifario("91 A MAS", " $ 1.44", "$ 3.50"))
                .append("[L]--------------------------------\n")
                .append("[C]DATOS DEL CLIENTE\n")
                .append("[L]--------------------------------\n")
                .append("[L]NUM DE CUENTA: <u><font size='big'>EE1286</font></u>\n")
                .append("[L]NOMBRE:\n")
                .append("[C]SALVADOR GARCIA FUENTES\n")
                .append("[L]DOCUMENTO: 123456789 \n")
                .append("[L]DIRECCION: \n")
                .append("[L]$direccionCliente\n")
                .append("[L]--------------------------------\n")
                .append("[C]DETALLE DEL DOCUMENTO\n")
                .append("[L]--------------------------------\n")
                .append(String.format("[L]%-7s%7s%7s%7S\n", "L.ACT.", "L.ANTE.", " CONS. M3.", "VALOR"))
                .append(filaTablaLecturas("35M3", "24M3", "11M3", "   $ ${String.format("%.2f", 0.72)}"))
                .append("[L]--------------------------------\n")
                .append(String.format("[L]%-12s%3s%6s%6S\n", "", "", "  PRECIO", ""))
                .append(String.format("[L]%-12s%3s%6s%6S\n", "  DESCRIP", "CANT", "UNI.", "  TOTAL"))
                .append("[L]--------------------------------\n")
                .append(filaTablaTotales("COST. ADMIN", "1", "$ ${String.format("%.2f", 3.58)}", "$ ${String.format("%.2f", 3.58)}"))
                .append(filaTablaTotales("MANTTO. SIST. AGUA POTABLE", "1", "$ ${String.format("%.2f", 4.42)}", "$ ${String.format("%.2f", 4.42)}"))
                .append(filaTablaTotales("CONSU. AGUA M3", "3", "$ ${String.format("%.2f", 0.24)}", "$ ${String.format("%.2f", 0.72)}"))
                .append(filaTablaTotales("CANON POR M3", "3", "$ ${String.format("%.2f", 0.07)}", "$ ${String.format("%.2f", 0.21)}"))
                .append(filaTablaTotales("MANTTO ALCANT. SANITARIO", "1", "$ ${String.format("%.2f", 2.00)}", "$ ${String.format("%.2f", 2.00)}"))
                .append(filaTablaTotales("PAGO DE NOVIEMBRE", "1", "\$ ${String.format("%.2f", 0.00)}", "$ ${String.format("%.2f", 0.00)}"))
                .append("[L]--------------------------------\n")
                .append(filaTablaTotales("TOTAL", "", "", "$ ${String.format("%.2f", 10.93)}"))
                .append("[L]--------------------------------\n\n")
                .append("[L]ENTREGADO POR: ANTONIO HERNANDEZ\n")
                .append("[C]FECHA: 02-01-2026 \n")
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
            "[L]%-23s%9s\n",
            desc.take(23),
            valor.take(9)
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

    private fun filaTablaLecturas(lActual: String, lAnterior: String, consumo: String, total:String): String {
        return String.format(
            "[L]%-7s%7s%7s%9s\n",
            lActual.take(7),
            lAnterior.take(7),
            consumo.take(7),
            total.take(9)
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

}