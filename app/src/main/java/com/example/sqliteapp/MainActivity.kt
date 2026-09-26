package com.example.sqliteapp

import android.os.Bundle
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sqliteapp.model.Categoria
import com.example.sqliteapp.model.Productos

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var managerCategoria: Categoria
    private lateinit var managerProductos: Productos

    private lateinit var txtIdDB: TextView
    private lateinit var txtId: EditText
    private lateinit var txtNombre: EditText
    private lateinit var txtPrecio: EditText
    private lateinit var txtCantidad: EditText
    private lateinit var cmbCategorias: Spinner

    private lateinit var btnAgregar: Button
    private lateinit var btnActualizar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnBuscar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Sesion.autenticada) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtIdDB = findViewById(R.id.txtIdDB)
        txtId = findViewById(R.id.txtId)
        txtNombre = findViewById(R.id.txtNombre)
        txtPrecio = findViewById(R.id.txtPrecio)
        txtCantidad = findViewById(R.id.txtCantidad)
        cmbCategorias = findViewById(R.id.cmbCategorias)
        btnAgregar = findViewById(R.id.btnAgregar)
        btnActualizar = findViewById(R.id.btnActualizar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnBuscar = findViewById(R.id.btnBuscar)

        managerCategoria = Categoria(this)
        managerProductos = Productos(this)
        setSpinnerCategorias()

        btnAgregar.setOnClickListener(this)
        btnActualizar.setOnClickListener(this)
        btnEliminar.setOnClickListener(this)
        btnBuscar.setOnClickListener(this)
    }

    fun setSpinnerCategorias() {
        managerCategoria.insertValuesDefault()
        val categorias = ArrayList<String>()
        managerCategoria.showAllCategoria().use { cursor ->
            while (cursor.moveToNext()) {
                categorias.add(cursor.getString(1))
            }
        }

        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cmbCategorias.adapter = adaptador
    }

    override fun onClick(view: View) {
        try {
            procesarOperacion(view)
        } catch (error: SQLiteException) {
            Toast.makeText(this, "No se pudo completar la operacion en la base de datos", Toast.LENGTH_LONG).show()
        }
    }

    private fun procesarOperacion(view: View) {
        val operacion = when (view.id) {
            R.id.btnAgregar -> "insertar"
            R.id.btnActualizar -> "actualizar"
            R.id.btnEliminar -> "eliminar"
            R.id.btnBuscar -> "buscar"
            else -> return
        }

        if (!vericarFormulario(operacion)) return

        if (operacion == "buscar") {
            buscarProducto(txtId.text.toString().trim().toInt())
            return
        }

        if (operacion == "eliminar") {
            val filas = managerProductos.deleteProducto(txtId.text.toString().trim().toInt())
            if (filas > 0) {
                txtId.text.clear()
                txtIdDB.text = ""
                txtNombre.text.clear()
                txtPrecio.text.clear()
                txtCantidad.text.clear()
                cmbCategorias.setSelection(if (cmbCategorias.count > 0) 0 else -1)
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_LONG).show()
            } else {
                mostrarProductoInexistente()
            }
            return
        }

        val nombre = txtNombre.text.toString().trim()
        val precio = txtPrecio.text.toString().trim().toDouble()
        val cantidad = txtCantidad.text.toString().trim().toInt()
        val categoria = cmbCategorias.selectedItem?.toString()
        val idcategoria = categoria?.let { managerCategoria.searchID(it) }
        if (idcategoria == null) {
            Toast.makeText(this, "Seleccione una categoria valida", Toast.LENGTH_LONG).show()
            return
        }

        if (operacion == "insertar") {
            managerProductos.addNewProducto(idcategoria, nombre, precio, cantidad)
            Toast.makeText(this, "Producto agregado", Toast.LENGTH_LONG).show()
        } else {
            val idproducto = txtId.text.toString().trim().toInt()
            val filas = managerProductos.updateProducto(idproducto, idcategoria, nombre, precio, cantidad)
            if (filas > 0) {
                txtIdDB.text = idproducto.toString()
                Toast.makeText(this, "Producto actualizado", Toast.LENGTH_LONG).show()
            } else {
                mostrarProductoInexistente()
            }
        }
    }

    private fun mostrarProductoInexistente() {
        txtIdDB.text = ""
        txtId.error = "No existe un producto con este codigo"
        txtId.requestFocus()
        Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_LONG).show()
    }

    private fun buscarProducto(id: Int) {
        managerProductos.searchProducto(id).use { cursor ->
            if (!cursor.moveToFirst()) {
                txtIdDB.text = ""
                txtNombre.text.clear()
                txtPrecio.text.clear()
                txtCantidad.text.clear()
                cmbCategorias.setSelection(-1)
                txtId.error = "No existe un producto con este codigo"
                Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_LONG).show()
                return
            }

            txtIdDB.text = cursor.getInt(cursor.getColumnIndexOrThrow(Productos.COL_ID)).toString()
            txtNombre.setText(cursor.getString(cursor.getColumnIndexOrThrow(Productos.COL_DESCRIPCION)))
            txtPrecio.setText(cursor.getDouble(cursor.getColumnIndexOrThrow(Productos.COL_PRECIO)).toString())
            val cantidadIndex = cursor.getColumnIndexOrThrow(Productos.COL_CANTIDAD)
            txtCantidad.setText(if (cursor.isNull(cantidadIndex)) "" else cursor.getInt(cantidadIndex).toString())

            val idcategoria = cursor.getInt(cursor.getColumnIndexOrThrow(Productos.COL_IDCATEGORIA))
            val categoria = managerCategoria.searchNombre(idcategoria)
            val posicion = (0 until cmbCategorias.count).firstOrNull {
                cmbCategorias.getItemAtPosition(it).toString() == categoria
            }
            cmbCategorias.setSelection(posicion ?: -1)
            Toast.makeText(this, "Producto encontrado", Toast.LENGTH_LONG).show()
        }
    }

    private fun vericarFormulario(opc: String): Boolean {
        txtId.error = null
        txtNombre.error = null
        txtPrecio.error = null
        txtCantidad.error = null
        var primerError: EditText? = null

        fun marcarError(campo: EditText, mensaje: String) {
            campo.error = mensaje
            if (primerError == null) primerError = campo
        }

        if (opc == "actualizar" || opc == "eliminar" || opc == "buscar") {
            val idproducto = txtId.text.toString().trim().toIntOrNull()
            if (idproducto == null || idproducto <= 0) {
                marcarError(txtId, "Ingrese un codigo de producto valido")
            }
        }

        if (opc == "insertar" || opc == "actualizar") {
            if (txtNombre.text.toString().trim().isEmpty()) {
                marcarError(txtNombre, "Ingrese el nombre del producto")
            }
            val precio = txtPrecio.text.toString().trim().toDoubleOrNull()
            if (precio == null || !precio.isFinite() || precio < 0) {
                marcarError(txtPrecio, "Ingrese un precio valido mayor o igual a cero")
            }
            val cantidad = txtCantidad.text.toString().trim().toIntOrNull()
            if (cantidad == null || cantidad < 0) {
                marcarError(txtCantidad, "Ingrese una cantidad inicial valida mayor o igual a cero")
            }
        }

        if (primerError != null) {
            primerError?.requestFocus()
            Toast.makeText(this, "Revise los campos indicados", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}
