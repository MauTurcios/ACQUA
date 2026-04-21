package com.sismantec.acqua.database

class Tablas {
    fun tbClientes(): String {
        return  "CREATE TABLE Clientes(" +
                "Id INTEGER  PRIMARY KEY NOT NULL," +
                "Codigo VARCHAR(25)  NOT NULL," +
                "Cliente varchar(200) NOT NULL," +
                "Casa Varchar(50)  NULL,"+
                "Poligono Varchar(50)  NULL,"+
                "Id_ruta"+
                "Codigo_casa Varchar(100)  NULL,"+
                "Direccion Varchar(200) NULL);"
    }
    fun tbRutas(): String{
        return "CREATE TABLE Rutas("+
                "Id INTEGER PRIMARY KEY NOT NULL," +
                "Ruta VARCHAR(50) NOT NULL);"
    }
    fun tbConfig(): String{
        return "CREATE TABLE Config("+
                "Id INTEGER PRIMARY KEY NOT NULL," +
                "dtePais Varchar(4)  NULL,"+
                "dteDepto Varchar(2)  NULL,"+
                "dteMunicipio Varchar(2)  NULL,"+
                "dteDistrito Varchar(4)  NULL,"+
                "dteNit Varchar(14)  NULL,"+
                "dteNrc Varchar(8)  NULL,"+
                "dteNombreEmisor Varchar(250)  NULL,"+
                "dteGiro Varchar(150)  NULL,"+
                "dteNombreComercial Varchar(150)  NULL,"+
                "dteDireccion Varchar(200)  NULL,"+
                "dteTelefono Varchar(30)  NULL,"+
                "dteCorreo Varchar(100)  NULL);"
    }
    fun tbVersionApp(): String{
        return "CREATE TABLE VersionApp("+
                "Id INTEGER PRIMARY KEY NOT NULL," +
                "VersionApp VARCHAR(25) NULL);"
    }
    fun empleados(): String {
        return "CREATE TABLE empleado(" +
                "id_empleado INTEGER NOT NULL," +
                "nombre_empleado VARCHAR(50) NOT NULL);"
    }

}