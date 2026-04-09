package com.sismantec.acqua.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.sismantec.acqua.entities.ClientesEntity


@Dao
interface ClientesDAO {

    //Insertando Clientes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarClientes(item: List<ClientesEntity>)

    //Seleccionando clientes
    @Query("SELECT * FROM Clientes ORDER BY Codigo")
    fun obtenerClientes(): Flow<List<ClientesEntity>>

    //ELIMINAR CLIENTES
    @Query("DELETE FROM Clientes")
    suspend fun eliminarClientes()

    //CONSULTAR CLIENTE POR ID
    @Query("SELECT * FROM Clientes WHERE Id = :id")
    suspend fun obtenerClienteId(id: Int): ClientesEntity
}