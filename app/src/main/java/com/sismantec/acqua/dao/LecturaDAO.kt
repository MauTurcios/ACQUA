package com.sismantec.acqua.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sismantec.acqua.entities.LecturaEntity

@Dao
interface LecturaDAO {

    @Insert
    suspend fun insertar(lectura: LecturaEntity)

    @Query("SELECT * FROM lectura ORDER BY Id ASC")
    suspend fun obtenerTodas(): List<LecturaEntity>

    /*
    @Query("""
        SELECT
            C.Codigo AS codigoCliente,
            C.Codigo_casa AS casa,
            L.Consumo AS consumo
        FROM lectura L
        INNER JOIN clientes C 
            ON c.Id = L.Id_cliente
        ORDER BY L.id ASC
    """
    )
    suspend fun obtenerLecturasResumen(): List<LecturaResumen>
     */

    @Query(
        """
                SELECT COUNT(*)
                FROM lectura
                WHERE Cuenta = :cuenta
                AND Lectura_enviada = 0
        """
    )
    suspend fun existeLecturaPendiente(cuenta: String): Int

    @Query("DELETE FROM lectura")
    suspend fun borrarAvisos()

    @Query("SELECT * FROM lectura WHERE id = :id")
    suspend fun obtenerLecturaId(id: Int): LecturaEntity?

    @Query("""
    UPDATE lectura
    SET Nombre = :nombre,
        Lectura_anterior = :lecturaAnterior,
        Lectura_actual = :lecturaActual,
        Consumo = :consumo,
        Usuario = :usuario,
        Documento = :documento,
        Direccion =:direccion,
        IdColbar = :idColbar,
        Colbar = :colbar,
        IdSector = :idSector,
        Sector = :sector,
        IdZona = :idZona,
        Zona = :zona,
        Lectura_enviada = 1,
        --CARGOS FIJOS 
        Cf1_linea = :cf1_linea,
        Cf1_descripcion= :cf1_descripcion,
        Cf1_precio = :cf1_precio,
        Cf2_linea = :cf2_linea,
        Cf2_descripcion= :cf2_descripcion,
        Cf2_precio = :cf2_precio,
        Cf3_linea = :cf3_linea,
        Cf3_descripcion= :cf3_descripcion,
        Cf3_precio = :cf3_precio, 
        Cf4_linea = :cf4_linea,
        Cf4_descripcion= :cf4_descripcion,
        Cf4_precio = :cf4_precio,
        Cf5_linea = :cf5_linea,
        Cf5_descripcion= :cf5_descripcion,
        Cf5_precio = :cf5_precio,
        --PLIEGO TARIFARIO
        Minimo_m3 = :minimo_m3,
        Primeros_m3 = :primeros_m3,
        Primeros_m3_valor = :primeros_m3_valor,
        E1_minimo_m3 = :e1_minimo_m3,
        E1_maximo_m3 = :e1_maximo_m3,
        E1_valor_m3 = :e1_valor_m3,
        E2_minimo_m3 = :e2_minimo_m3,
        E2_maximo_m3 = :e2_maximo_m3,
        E2_valor_m3 = :e2_valor_m3,
        E3_minimo_m3 = :e3_minimo_m3,
        E3_maximo_m3 = :e3_maximo_m3,
        E3_valor_m3 = :e3_valor_m3,
        E4_minimo_m3 = :e4_minimo_m3,
        E4_maximo_m3 = :e4_maximo_m3,
        E4_valor_m3 = :e4_valor_m3,
        E5_minimo_m3 = :e5_minimo_m3,
        E5_maximo_m3 = :e5_maximo_m3,
        E5_valor_m3 = :e5_valor_m3,
        E6_minimo_m3 = :e6_minimo_m3,
        E6_maximo_m3 = :e6_maximo_m3,
        E6_valor_m3 = :e6_valor_m3,
        E7_minimo_m3 = :e7_minimo_m3,
        E7_maximo_m3 = :e7_maximo_m3,
        E7_valor_m3 = :e7_valor_m3,
        E8_minimo_m3 = :e8_minimo_m3,
        E8_maximo_m3 = :e8_maximo_m3,
        E8_valor_m3 = :e8_valor_m3, 
        E9_minimo_m3 = :e9_minimo_m3,
        E9_maximo_m3 = :e9_maximo_m3,
        E9_valor_m3 = :e9_valor_m3,
        E10_minimo_m3 = :e10_minimo_m3,
        E10_maximo_m3 = :e10_maximo_m3,
        E10_valor_m3 = :e10_valor_m3
    WHERE id = :id
""")
    suspend fun marcarLecturaEnviada(
        id: Int,
        nombre: String,
        lecturaAnterior: Double,
        lecturaActual: Double,
        consumo: Int,
        usuario: String,
        documento: String,
        direccion: String,
        idColbar: Int,
        colbar: String,
        idSector: Int,
        sector: String,
        idZona: Int,
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
        e10_valor_m3: Double
    )

}