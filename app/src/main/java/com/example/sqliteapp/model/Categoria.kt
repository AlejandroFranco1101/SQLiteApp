package com.example.sqliteapp.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.sqliteapp.db.HelperDB

class Categoria(context: Context?) {

    private val helper: HelperDB = HelperDB(context)
    private val db: SQLiteDatabase = helper.writableDatabase

    companion object {
        const val TABLE_NAME_CATEGORIA = "categoria"
        const val COL_ID = "idcategoria"
        const val COL_NOMBRE = "nombre"

        const val CREATE_TABLE_CATEGORIA =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_CATEGORIA + "(" +
                COL_ID + " integer primary key autoincrement," +
                COL_NOMBRE + " varchar(50) NOT NULL);"
    }

    fun generarContentValues(nombre: String?): ContentValues {
        val valores = ContentValues()
        valores.put(COL_NOMBRE, nombre)
        return valores
    }

    fun insertValuesDefault() {
        val categories = arrayOf(
            "Abarrotes",
            "Carnes",
            "Embutidos",
            "Mariscos",
            "Pescado",
            "Bebidas",
            "Verduras",
            "Frutas",
            "Bebidas Carbonatadas",
            "Bebidas no carbonatadas"
        )

        val columns = arrayOf(COL_ID, COL_NOMBRE)
        val tieneCategorias = db.query(
            TABLE_NAME_CATEGORIA, columns, null, null, null, null, null
        ).use { cursor ->
            cursor.moveToFirst()
        }

        // Cargar las categorias solamente cuando la tabla esta vacia.
        if (!tieneCategorias) {
            for (item in categories) {
                db.insert(TABLE_NAME_CATEGORIA, null, generarContentValues(item))
            }
        }
    }

    fun showAllCategoria(): Cursor {
        val columns = arrayOf(COL_ID, COL_NOMBRE)
        return db.query(
            TABLE_NAME_CATEGORIA, columns,
            null, null, null, null, "$COL_NOMBRE ASC"
        )
    }

    // Recuperar el ID a partir del nombre seleccionado en el Spinner.
    fun searchID(nombre: String): Int? {
        val columns = arrayOf(COL_ID, COL_NOMBRE)
        return db.query(
            TABLE_NAME_CATEGORIA, columns,
            "$COL_NOMBRE=?", arrayOf(nombre), null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else null
        }
    }

    fun searchNombre(id: Int): String? {
        val columns = arrayOf(COL_ID, COL_NOMBRE)
        return db.query(
            TABLE_NAME_CATEGORIA, columns,
            "$COL_ID=?", arrayOf(id.toString()), null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(1) else null
        }
    }
}
