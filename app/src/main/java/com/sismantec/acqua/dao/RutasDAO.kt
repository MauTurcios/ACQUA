package com.sismantec.acqua.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sismantec.acqua.entities.ClientesEntity
import kotlinx.coroutines.flow.Flow
import com.sismantec.acqua.entities.RutasEntity

@Dao
interface RutasDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRutas(item: List<RutasEntity>)

    @Query("SELECT * FROM Rutas ORDER BY Ruta ASC")
    fun obtenerRutas(): Flow<List<RutasEntity>>
}