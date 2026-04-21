package com.sismantec.acqua.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sismantec.acqua.database.Tablas

class Database(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION) {

    private var tabla : Tablas = Tablas()

    companion object{
        private const val DATABASE_VERSION =1 //version de la bd
        private const val DATABASE_NAME = "acqua.db" //nombre de la bd
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(tabla.tbClientes())//ejecuta la tabla clientes
        db?.execSQL(tabla.tbRutas())// ejecuta tabla rutas
        db?.execSQL(tabla.tbConfig())// ejecuta tabla config
        //db?.execSQL(tabla.tbVersionApp()) // ejecuta tabla version app
        db?.execSQL(tabla.empleados()) // ejecuta tabla empleados
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        if(oldVersion < newVersion){
            //CREANDO TABLA SIN ELIMINAR LA ANTERIOR BD
            //HABILITAR CON LA VERSION DE LA BD EN 2
            db?.execSQL(tabla.tbClientes())
            db?.execSQL(tabla.tbRutas())
            db?.execSQL(tabla.tbConfig())
            db?.execSQL(tabla.empleados())
        }
    }
}