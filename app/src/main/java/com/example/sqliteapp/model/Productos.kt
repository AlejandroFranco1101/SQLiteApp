package com.example.sqliteapp.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.sqliteapp.db.HelperDB

class Productos(context: Context?) {

    private val helper: HelperDB = HelperDB(context)
    private val db: SQLiteDatabase = helper.writableDatabase

    companion object {
        const val TABLE_NAME_PRODUCTOS = "productos"
        const val COL_ID = "idproductos"
        const val COL_IDCATEGORIA = "idcategoria"
        const val COL_DESCRIPCION = "descripcion"
        const val COL_PRECIO = "precio"
        const val COL_CANTIDAD = "cantidad"

        const val CREATE_TABLE_PRODUCTOS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_PRODUCTOS + "(" +
                COL_ID + " integer primary key autoincrement," +
                COL_IDCATEGORIA + " integer NOT NULL," +
                COL_DESCRIPCION + " varchar(150) NOT NULL," +
                COL_PRECIO + " decimal(10,2) NOT NULL," +
                COL_CANTIDAD + " integer," +
                "FOREIGN KEY(idcategoria) REFERENCES categoria(idcategoria));"
    }

    fun generarContentValues(
        idcategoria: Int?,
        descripcion: String?,
        precio: Double?,
        cantidad: Int?
    ): ContentValues {
        val valores = ContentValues()
        valores.put(COL_IDCATEGORIA, idcategoria)
        valores.put(COL_DESCRIPCION, descripcion)
        valores.put(COL_PRECIO, precio)
        valores.put(COL_CANTIDAD, cantidad)
        return valores
    }

    // Agregar un nuevo registro.
    fun addNewProducto(
        idcategoria: Int?,
        descripcion: String?,
        precio: Double?,
        cantidad: Int?
    ) {
        db.insert(
            TABLE_NAME_PRODUCTOS,
            null,
            generarContentValues(idcategoria, descripcion, precio, cantidad)
        )
    }

    // Eliminar un registro por su ID.
    fun deleteProducto(id: Int) {
        db.delete(TABLE_NAME_PRODUCTOS, "$COL_ID=?", arrayOf(id.toString()))
    }

    // Modificar un registro por su ID.
    fun updateProducto(
        id: Int,
        idcategoria: Int?,
        descripcion: String?,
        precio: Double?,
        cantidad: Int?
    ) {
        db.update(
            TABLE_NAME_PRODUCTOS,
            generarContentValues(idcategoria, descripcion, precio, cantidad),
            "$COL_ID=?", arrayOf(id.toString())
        )
    }

    // El llamador debe cerrar el cursor cuando termine de leerlo.
    fun searchProducto(id: Int): Cursor {
        val columns = arrayOf(COL_ID, COL_IDCATEGORIA, COL_DESCRIPCION, COL_PRECIO, COL_CANTIDAD)
        return db.query(
            TABLE_NAME_PRODUCTOS, columns,
            "$COL_ID=?", arrayOf(id.toString()), null, null, null
        )
    }

    // Mostrar todos los registros ordenados por descripcion.
    fun searchProductosAll(): Cursor {
        val columns = arrayOf(COL_ID, COL_IDCATEGORIA, COL_DESCRIPCION, COL_PRECIO, COL_CANTIDAD)
        return db.query(
            TABLE_NAME_PRODUCTOS, columns,
            null, null, null, null, "$COL_DESCRIPCION ASC"
        )
    }
}
