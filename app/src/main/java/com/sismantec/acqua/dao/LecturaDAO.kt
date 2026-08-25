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
        Lectura_enviada = 1
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
        cf5_precio: Double
    )

}