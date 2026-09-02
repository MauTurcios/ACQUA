package com.sismantec.acqua.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sismantec.acqua.database.AppDataBase
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.models.ConsumoResponse

import kotlinx.coroutines.launch

class lecturaViewModel (application: Application): AndroidViewModel(application) {

    private val db = AppDataBase.obtenerInstancia(application)
    private val lecturaDAO = db.LecturaDAO()

    fun guardarLectura(datos: ConsumoResponse){
        viewModelScope.launch {
            val lectura = LecturaEntity(
                Cuenta = datos.cuenta,
                Nombre = datos.nombre,
                Periodo = datos.periodo,
                Lectura_anterior = datos.lecturaAnterior,
                Lectura_actual = datos.lecturaActual,
                Consumo = datos.consumo,
                Usuario = datos.usuario,
                Documento = datos.documento,
                Direccion = datos.direccion,
                IdColbar = datos.idColbar,
                Colbar = datos.colbar,
                IdSector = datos.idSector,
                Sector = datos.sector,
                IdZona = datos.idZona,
                Zona = datos.zona,
                Lectura_enviada = true,
                //CARGOS FIJOS
                Cf1_linea = datos.cf1_linea,
                Cf1_descripcion = datos.cf1_descripcion,
                Cf1_precio = datos.cf1_precio,
                Cf2_linea = datos.cf2_linea,
                Cf2_descripcion = datos.cf2_descripcion,
                Cf2_precio = datos.cf2_precio,
                Cf3_linea = datos.cf3_linea,
                Cf3_descripcion = datos.cf3_descripcion,
                Cf3_precio = datos.cf3_precio,
                Cf4_linea = datos.cf4_linea,
                Cf4_descripcion = datos.cf4_descripcion,
                Cf4_precio = datos.cf4_precio,
                Cf5_linea = datos.cf5_linea,
                Cf5_descripcion = datos.cf5_descripcion,
                Cf5_precio = datos.cf5_precio,
                //PLIEGO TARIFARIO
                Minimo_m3 = datos.minimo_m3,
                Primeros_m3 = datos.primeros_m3,
                Primeros_m3_valor = datos.primeros_m3_valor,
                E1_minimo_m3 = datos.e1_minimo_m3,
                E1_maximo_m3 = datos.e1_maximo_m3,
                E1_valor_m3 = datos.e1_valor_m3,
                E2_minimo_m3 = datos.e2_minimo_m3,
                E2_maximo_m3 = datos.e2_maximo_m3,
                E2_valor_m3 = datos.e2_valor_m3,
                E3_minimo_m3 = datos.e3_minimo_m3,
                E3_maximo_m3 = datos.e3_maximo_m3,
                E3_valor_m3 = datos.e3_valor_m3,
                E4_minimo_m3 = datos.e4_minimo_m3,
                E4_maximo_m3 = datos.e4_maximo_m3,
                E4_valor_m3 = datos.e4_valor_m3,
                E5_minimo_m3 = datos.e5_minimo_m3,
                E5_maximo_m3 = datos.e5_maximo_m3,
                E5_valor_m3 = datos.e5_valor_m3,
                E6_minimo_m3 = datos.e6_minimo_m3,
                E6_maximo_m3 = datos.e6_maximo_m3,
                E6_valor_m3 = datos.e6_valor_m3,
                E7_minimo_m3 = datos.e7_minimo_m3,
                E7_maximo_m3 = datos.e7_maximo_m3,
                E7_valor_m3 = datos.e7_valor_m3,
                E8_minimo_m3 = datos.e8_minimo_m3,
                E8_maximo_m3 = datos.e8_maximo_m3,
                E8_valor_m3 = datos.e8_valor_m3,
                E9_minimo_m3 = datos.e9_minimo_m3,
                E9_maximo_m3 = datos.e9_maximo_m3,
                E9_valor_m3 = datos.e9_valor_m3,
                E10_minimo_m3 = datos.e10_minimo_m3,
                E10_maximo_m3 = datos.e10_maximo_m3,
                E10_valor_m3 = datos.e10_valor_m3
            )
            lecturaDAO.insertar(lectura)
        }
    }

    fun obtenerLecturas(callback: (List<LecturaEntity>) -> Unit){
        viewModelScope.launch {
            val lista = lecturaDAO.obtenerTodas()
            callback(lista)
        }
    }

    fun existeLecturaPendiente(cuenta: String, callback:(Boolean) -> Unit){
        viewModelScope.launch {
            val cantidad = lecturaDAO.existeLecturaPendiente(cuenta)
            callback(cantidad >0)
        }
    }

    fun borrarAvisoCobro(){
        viewModelScope.launch {
            lecturaDAO.borrarAvisos()
        }
    }

    fun obtenerLecturaId(id: Int, callback: (LecturaEntity?) -> Unit){
        viewModelScope.launch {
            val lectura = lecturaDAO.obtenerLecturaId(id)
            callback(lectura)
        }
    }

    fun marcarLecturaEnviada(
        id: Int,
        nombre: String,
        lecturaAnterior: Double,
        lecturaActual: Double,
        consumo: Int,
        usuario: String,
        documento: String,
        direccion: String,
        idcolbar: Int,
        colbar: String,
        idsector: Int,
        sector: String,
        idzona: Int,
        zona: String,
        //CARGOS FIJOS
        cf1_linea: String,
        cf1_descripcion: String,
        cf1_precio: Double,
        cf2_linea: String,
        cf2_descripcion: String,
        cf2_precio: Double,
        cf3_linea: String,
        cf3_descripcion: String,
        cf3_precio: Double,
        cf4_linea: String,
        cf4_descripcion: String,
        cf4_precio: Double,
        cf5_linea: String,
        cf5_descripcion: String,
        cf5_precio: Double,
        //PLIEGO TARIFARIO
        minimo_m3: Int,
        primeros_m3: Double,
        primeros_m3_valor: Double,
        e1_minimo_m3: Double,
        e1_maximo_m3: Double,
        e1_valor_m3: Double,
        e2_minimo_m3: Double,
        e2_maximo_m3: Double,
        e2_valor_m3: Double,
        e3_minimo_m3: Double,
        e3_maximo_m3: Double,
        e3_valor_m3: Double,
        e4_minimo_m3: Double,
        e4_maximo_m3: Double,
        e4_valor_m3: Double,
        e5_minimo_m3: Double,
        e5_maximo_m3: Double,
        e5_valor_m3: Double,
        e6_minimo_m3: Double,
        e6_maximo_m3: Double,
        e6_valor_m3: Double,
        e7_minimo_m3: Double,
        e7_maximo_m3: Double,
        e7_valor_m3: Double,
        e8_minimo_m3: Double,
        e8_maximo_m3: Double,
        e8_valor_m3: Double,
        e9_minimo_m3: Double,
        e9_maximo_m3: Double,
        e9_valor_m3: Double,
        e10_minimo_m3: Double,
        e10_maximo_m3: Double,
        e10_valor_m3: Double,
        callback: () -> Unit
    ) {
        viewModelScope.launch {
            lecturaDAO.marcarLecturaEnviada(
                id = id,
                nombre = nombre,
                lecturaAnterior = lecturaAnterior,
                lecturaActual = lecturaActual,
                consumo = consumo,
                usuario = usuario,
                documento = documento,
                direccion = direccion,
                idColbar = idcolbar,
                colbar = colbar,
                idSector = idsector,
                sector = sector,
                idZona = idzona,
                zona = zona,
                cf1_linea = cf1_linea,
                cf1_descripcion = cf1_descripcion,
                cf1_precio = cf1_precio,
                cf2_linea = cf2_linea,
                cf2_descripcion = cf2_descripcion,
                cf2_precio = cf2_precio,
                cf3_linea = cf3_linea,
                cf3_descripcion = cf3_descripcion,
                cf3_precio = cf3_precio,
                cf4_linea = cf4_linea,
                cf4_descripcion = cf4_descripcion,
                cf4_precio = cf4_precio,
                cf5_linea = cf5_linea,
                cf5_descripcion = cf5_descripcion,
                cf5_precio = cf5_precio,
                minimo_m3 = minimo_m3,
                primeros_m3 = primeros_m3,
                primeros_m3_valor = primeros_m3_valor,
                e1_minimo_m3 = e1_minimo_m3,
                e1_maximo_m3 = e1_maximo_m3,
                e1_valor_m3 = e1_valor_m3,
                e2_minimo_m3 = e2_minimo_m3,
                e2_maximo_m3 = e2_maximo_m3,
                e2_valor_m3 = e2_valor_m3,
                e3_minimo_m3 = e3_minimo_m3,
                e3_maximo_m3 = e3_maximo_m3,
                e3_valor_m3 = e3_valor_m3,
                e4_minimo_m3 = e4_minimo_m3,
                e4_maximo_m3 = e4_maximo_m3,
                e4_valor_m3 = e4_valor_m3,
                e5_minimo_m3 = e5_minimo_m3,
                e5_maximo_m3 = e5_maximo_m3,
                e5_valor_m3 = e5_valor_m3,
                e6_minimo_m3 = e6_minimo_m3,
                e6_maximo_m3 = e6_maximo_m3,
                e6_valor_m3 = e6_valor_m3,
                e7_minimo_m3 = e7_minimo_m3,
                e7_maximo_m3 = e7_maximo_m3,
                e7_valor_m3 = e7_valor_m3,
                e8_minimo_m3 = e8_minimo_m3,
                e8_maximo_m3 = e8_maximo_m3,
                e8_valor_m3 = e8_valor_m3,
                e9_minimo_m3 = e9_minimo_m3,
                e9_maximo_m3 = e9_maximo_m3,
                e9_valor_m3 = e9_valor_m3,
                e10_minimo_m3 = e10_minimo_m3,
                e10_maximo_m3 = e10_maximo_m3,
                e10_valor_m3 = e10_valor_m3
            )
            callback()
        }
    }

}