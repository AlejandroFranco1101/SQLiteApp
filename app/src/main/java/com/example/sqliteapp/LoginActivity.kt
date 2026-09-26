package com.example.sqliteapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sqliteapp.model.Usuario

class LoginActivity : AppCompatActivity() {
    private lateinit var nick: EditText
    private lateinit var clave: EditText
    private lateinit var confirmar: EditText
    private lateinit var aceptar: Button
    private lateinit var cambiar: Button
    private var registro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        nick = findViewById(R.id.txtNick)
        clave = findViewById(R.id.txtClave)
        confirmar = findViewById(R.id.txtConfirmar)
        aceptar = findViewById(R.id.btnIngresar)
        cambiar = findViewById(R.id.btnCambiarModo)
        registro = savedInstanceState?.getBoolean("registro") ?: false
        mostrarModo()
        cambiar.setOnClickListener {
            registro = !registro
            mostrarModo()
        }
        aceptar.setOnClickListener { enviar() }
    }

    override fun onResume() {
        super.onResume()
        Sesion.autenticada = false
        clave.text.clear()
        confirmar.text.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("registro", registro)
        super.onSaveInstanceState(outState)
    }

    private fun mostrarModo() {
        findViewById<TextView>(R.id.tituloLogin).text = if (registro) "Crear cuenta" else "Iniciar sesión"
        confirmar.visibility = if (registro) View.VISIBLE else View.GONE
        aceptar.text = if (registro) "Registrarse" else "Ingresar"
        cambiar.text = if (registro) "Ya tengo cuenta" else "Crear una cuenta"
        clave.text.clear()
        confirmar.text.clear()
        nick.error = null
        clave.error = null
        confirmar.error = null
    }

    private fun enviar() {
        val nombre = nick.text.toString().trim()
        val password = clave.text.toString()
        nick.error = null
        clave.error = null
        confirmar.error = null
        if (nombre.isEmpty()) {
            nick.error = "Ingrese su nick"
            nick.requestFocus()
            return
        }
        if (password.isBlank() || (registro && password.length < 8)) {
            clave.error = if (registro) "Use al menos 8 caracteres" else "Ingrese su contraseña"
            clave.requestFocus()
            return
        }
        if (registro && password != confirmar.text.toString()) {
            confirmar.error = "Las contraseñas no coinciden"
            confirmar.requestFocus()
            return
        }
        val creando = registro
        aceptar.isEnabled = false
        cambiar.isEnabled = false
        Thread {
            val resultado = runCatching {
                val usuario = Usuario(applicationContext)
                if (creando) usuario.registrar(nombre, password) else usuario.autenticar(nombre, password)
            }
            runOnUiThread {
                if (isDestroyed || isFinishing) return@runOnUiThread
                aceptar.isEnabled = true
                cambiar.isEnabled = true
                when {
                    resultado.isFailure -> Toast.makeText(this, "No se pudo acceder a la base de datos", Toast.LENGTH_LONG).show()
                    resultado.getOrDefault(false) && creando -> {
                        registro = false
                        mostrarModo()
                        Toast.makeText(this, "Cuenta creada. Inicie sesión", Toast.LENGTH_LONG).show()
                    }
                    resultado.getOrDefault(false) -> {
                        Sesion.autenticada = true
                        clave.text.clear()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    creando -> { nick.error = "Este nick ya está registrado"; nick.requestFocus() }
                    else -> { clave.error = "Nick o contraseña incorrectos"; clave.requestFocus() }
                }
            }
        }.start()
    }
}
