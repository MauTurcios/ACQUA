package com.sismantec.acqua.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sismantec.acqua.dao.ClientesDAO
import com.sismantec.acqua.dao.ConfigDAO
import com.sismantec.acqua.dao.LecturaDAO
import com.sismantec.acqua.dao.RutasDAO
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.entities.ConfigEntity
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.entities.RutasEntity

@Database(
    entities = [ClientesEntity::class, RutasEntity::class, ConfigEntity::class, LecturaEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun clientesDao(): ClientesDAO
    abstract fun RutasDAO(): RutasDAO
    abstract fun ConfigDAO(): ConfigDAO
    abstract fun LecturaDAO(): LecturaDAO

    companion object {
        const val DATABASE_NAME = "Acqua.db"

        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun obtenerInstancia(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        fun cerrarInstancia() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
