package com.sismantec.acqua.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sismantec.acqua.dao.ClientesDAO
import com.sismantec.acqua.dao.ConfigDAO
import com.sismantec.acqua.dao.LecturaDAO
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.entities.RutasEntity
import com.sismantec.acqua.dao.RutasDAO
import com.sismantec.acqua.entities.ConfigEntity
import com.sismantec.acqua.entities.LecturaEntity

@Database(
    entities = [ClientesEntity::class, RutasEntity::class, ConfigEntity::class, LecturaEntity::class]
    , version = 2
    , exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun clientesDao() : ClientesDAO
    abstract fun RutasDAO(): RutasDAO
    abstract fun ConfigDAO(): ConfigDAO
    abstract fun LecturaDAO(): LecturaDAO

    companion object{

        @Volatile
        private var INSTANCE: AppDataBase? = null
        fun obtenerInstancia(context: Context) : AppDataBase{

            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "Acqua.db"
                ).build().also {
                    INSTANCE = it
                }
            }

        }

    }

}