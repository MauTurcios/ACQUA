package com.sismantec.acqua.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.sismantec.acqua.entities.ConfigEntity

@Dao
interface ConfigDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarConfig(item: List<ConfigEntity>)

    @Query("SELECT * FROM Config LIMIT 1")
    fun obtenerConfig(): Flow<ConfigEntity>

    @Query("DELETE FROM Config")
    suspend fun eliminarConfig()
}