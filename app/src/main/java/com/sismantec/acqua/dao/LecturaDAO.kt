package com.sismantec.acqua.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.models.LecturaResumen

@Dao
interface LecturaDAO {

    @Insert
    suspend fun insertar(lectura: LecturaEntity)

    @Query("SELECT * FROM lectura ORDER BY Id ASC")
    suspend fun obtenerTodas(): List<LecturaEntity>

    @Query("""
        SELECT
            C.Codigo AS codigoCliente,
            C.Direccion AS direccion,
            L.Consumo AS consumo
        FROM lectura L
        INNER JOIN clientes C 
            ON c.Id = L.Id_cliente
        ORDER BY L.id ASC
    """
    )
    suspend fun obtenerLecturasResumen(): List<LecturaResumen>

    @Query("DELETE FROM lectura")
    suspend fun borrarAvisos()

}