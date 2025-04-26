package com.example.starfinder.services

import android.content.Context
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.FileChannel

fun copyDatabaseFromAssets(context: Context) {
    val dbFile = context.getDatabasePath("StarFinder.sqlite3") // имя базы данных в локальном хранилище

    // Удаляем старую базу данных перед копированием новой
    if (dbFile.exists()) {
        dbFile.delete()
        Log.d("DB", "Старая база данных удалена.")
    }

    // Создаём родительскую директорию для базы данных, если её нет
    dbFile.parentFile?.mkdirs()

    try {
        // Открываем поток для чтения базы данных из assets
        val inputStream: InputStream = context.assets.open("StarFinder.sqlite3") // Получаем AssetInputStream

        // Открываем поток для записи в локальное хранилище
        val outputStream: OutputStream = FileOutputStream(dbFile)

        // Копируем данные поблочно
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        inputStream.close()
        outputStream.close()

        Log.d("DB", "База данных успешно скопирована.")

    } catch (e: IOException) {
        Log.e("DB", "Ошибка копирования базы данных", e)
    }
}