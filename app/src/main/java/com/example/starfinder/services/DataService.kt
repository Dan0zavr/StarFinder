package com.example.starfinder.services

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.starfinder.models.User

class DataService(context: Context) : SQLiteOpenHelper(
    context,
    "StarFinder.db",
    null,
    1
) {

    override fun onCreate(db: SQLiteDatabase?) {
        // БД уже существует в assets — не создаём
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Миграции пока не нужны
    }

    fun insert(tableName: String, values: Map<String, Any?>): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues()

        values.forEach { (key, value) ->
            when (value) {
                is String -> contentValues.put(key, value)
                is Int -> contentValues.put(key, value)
                is Float -> contentValues.put(key, value)
                is Double -> contentValues.put(key, value)
                null -> contentValues.putNull(key)
                else -> Log.w("DB", "Unsupported type for $key: ${value?.javaClass}")
            }
        }

        val result = db.insert(tableName, null, contentValues)
        return result != -1L
    }

    fun update(tableName: String, values: Map<String, Any?>, whereClause: String, whereArgs: Array<String>): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues()

        values.forEach { (key, value) ->
            when (value) {
                is String -> contentValues.put(key, value)
                is Int -> contentValues.put(key, value)
                is Float -> contentValues.put(key, value)
                is Double -> contentValues.put(key, value)
                null -> contentValues.putNull(key)
                else -> Log.w("DB", "Unsupported type for $key: ${value?.javaClass}")
            }
        }

        val result = db.update(tableName, contentValues, whereClause, whereArgs)
        return result > 0
    }

    fun delete(tableName: String, whereClause: String, whereArgs: Array<String>): Boolean {
        val db = writableDatabase
        val result = db.delete(tableName, whereClause, whereArgs)
        return result > 0
    }

    fun getAll(tableName: String): Cursor {
        val db = readableDatabase
        return db.query(tableName, null, null, null, null, null, null)
    }

    fun getWithQuery(query: String, args: Array<String> = emptyArray()): Cursor {
        val db = readableDatabase
        return db.rawQuery(query, args)
    }

    fun getUserByEmailAndPassword(email: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            "User",
            null,
            "Email = ? AND Password = ?",
            arrayOf(email, password),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("UserId")),
                userName = cursor.getString(cursor.getColumnIndexOrThrow("UserName")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("Email")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("Password"))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM User WHERE Email = ?", arrayOf(email))

        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("UserId"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("UserName"))
            val mail = cursor.getString(cursor.getColumnIndexOrThrow("Email"))
            val pass = cursor.getString(cursor.getColumnIndexOrThrow("Password"))
            user = User(id, name, mail, pass)
        }
        cursor.close()
        return user
    }

    fun insertUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("UserName", user.userName)
            put("Email", user.email)
            put("Password", user.password)
        }
        val result = db.insert("User", null, values)
        return result != -1L
    }

    fun isEmailTaken(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            "User",
            arrayOf("UserId"),
            "Email = ?",
            arrayOf(email),
            null, null, null
        )
        val taken = cursor.count > 0
        cursor.close()
        return taken
    }
}
