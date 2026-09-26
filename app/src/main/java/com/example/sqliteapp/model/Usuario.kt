package com.example.sqliteapp.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Base64
import com.example.sqliteapp.db.HelperDB
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class Usuario(private val context: Context) {
    companion object {
        const val CREATE_TABLE_USUARIO = "CREATE TABLE IF NOT EXISTS Usuario (" +
            "idusuario INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nick TEXT NOT NULL COLLATE NOCASE UNIQUE," +
            "contrasena TEXT NOT NULL, salt TEXT NOT NULL);"
    }

    private fun derivar(contrasena: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(contrasena.toCharArray(), salt, 210000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun registrar(nick: String, contrasena: String): Boolean {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val valores = ContentValues().apply {
            put("nick", nick.trim())
            put("contrasena", Base64.encodeToString(derivar(contrasena, salt), Base64.NO_WRAP))
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
        }
        return HelperDB(context).use { helper ->
            try {
                helper.writableDatabase.insertOrThrow("Usuario", null, valores)
                true
            } catch (error: SQLiteConstraintException) {
                false
            }
        }
    }

    fun autenticar(nick: String, contrasena: String): Boolean = HelperDB(context).use { helper ->
        helper.readableDatabase.query(
            "Usuario", arrayOf("contrasena", "salt"), "nick=?",
            arrayOf(nick.trim()), null, null, null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return false
            val esperado = Base64.decode(cursor.getString(0), Base64.NO_WRAP)
            val salt = Base64.decode(cursor.getString(1), Base64.NO_WRAP)
            MessageDigest.isEqual(esperado, derivar(contrasena, salt))
        }
    }
}
