package com.example.starfinder.services

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getFloatOrNull
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.models.Observation
import com.example.starfinder.models.User

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


class DataService(context: Context) : SQLiteOpenHelper(
    context,
    "StarFinder.sqlite3",
    null,
    1
) {

    override fun onCreate(db: SQLiteDatabase?) {
        // БД уже существует в assets — не создаём
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Миграции пока не нужны
    }

    fun <T : Any> insert(tableName: String, entity: T, vararg excludedProperties: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            entity::class.memberProperties.forEach { prop ->
                if (prop.name !in excludedProperties) {
                    @Suppress("UNCHECKED_CAST")
                    val kProperty = prop as KProperty1<T, Any?>
                    try {
                        when (val value = prop.get(entity)) {
                            null -> putNull(prop.name)  // Используем putNull без префикса
                            is Int -> put(prop.name, value)  // Используем put без префикса
                            is Long -> put(prop.name, value)
                            is String -> put(prop.name, value)
                            is Boolean -> put(prop.name, value)
                            is Float -> put(prop.name, value)
                            is Double -> put(prop.name, value)
                            is ByteArray -> put(prop.name, value)
                            else -> Log.w("DB", "Unsupported type for ${prop.name}")
                        }
                    } catch (e: Exception) {
                        Log.e("DB", "Error processing property ${prop.name}", e)
                    }
                }
            }
        }

        return db.insert(tableName, null, values)
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

    fun <T> getWithQuery(query: String, args: Array<String> = emptyArray(), mapper: (Cursor) -> T): List<T>
    {
        val db = readableDatabase
        val cursor = db.rawQuery(query, args)
        return cursor.use {
            mutableListOf<T>().apply {
                while (cursor.moveToNext()) {
                    add(mapper(cursor))
                }
            }
        }
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

    fun getSourceLinkById(sourceId: Int): String? {
        val db = readableDatabase
        var link: String? = null

        val cursor = db.rawQuery(
            "SELECT Link FROM DataSource WHERE SourceId = ?",
            arrayOf(sourceId.toString())
        )

        if (cursor.moveToFirst()) {
            link = cursor.getString(cursor.getColumnIndexOrThrow("Link"))
        }

        cursor.close()
        return link
    }

    fun getCelestialBodiesForObservation(observationId: Int?): List<CelestialBody> {
        val db = readableDatabase
        val celestialBodies = mutableListOf<CelestialBody>()

        val query = """
        SELECT cb.* FROM CelestialBody cb
        JOIN CelestialBodyInObservation cbo ON cb.CelestialBodyId = cbo.celestialBodyId
        WHERE cbo.observationId = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(observationId.toString()))

        while (cursor.moveToNext()) {
            celestialBodies.add(
                CelestialBody(
                    celestialBodyId = cursor.getInt(cursor.getColumnIndexOrThrow("CelestialBodyId")),
                    celestialBodyName = cursor.getString(cursor.getColumnIndexOrThrow("CelestialBodyName")),
                    deflection = cursor.getFloat(cursor.getColumnIndexOrThrow("Deflection")),
                    ascension = cursor.getFloat(cursor.getColumnIndexOrThrow("Ascension")),
                    dataSourceId = cursor.getInt(cursor.getColumnIndexOrThrow("DataSourceId"))
                )
            )
        }
        cursor.close()

        return celestialBodies
    }

    fun getStarNameByObservation(observationId: Int?):String?{
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
    SELECT CelestialBody.CelestialBodyName
    FROM Observation
    JOIN CelestialBodyInObservation ON Observation.ObservationId = CelestialBodyInObservation.ObservationId
    JOIN CelestialBody ON CelestialBodyInObservation.CelestialBodyId = CelestialBody.CelestialBodyId
    WHERE Observation.ObservationId = ?
    """.trimIndent(),
            arrayOf(observationId.toString())
        )

        var starName: String? = null
        if (cursor.moveToFirst()) {
            starName = cursor.getString(cursor.getColumnIndexOrThrow("CelestialBodyName"))
        }
        cursor.close()

        return starName
    }

    fun checkDatabase(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        cursor.use {
            while (it.moveToNext()) {
                Log.d("DB_TABLES", it.getString(0))
            }
        }
        return cursor.count > 0
    }

}