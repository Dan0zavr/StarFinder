package com.example.starfinder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.starfinder.services.Api.ApiManager
import com.example.starfinder.services.SharedPreferences.SessionManager
import com.example.starfinder.services.copyDatabaseFromAssets

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        copyDatabaseFromAssets(this)
        ApiManager.init(this)

        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)


        // Исправлено имя переменной (было sharedPrefs, должно быть sharedPref)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        val userId = sharedPref.getInt("UserId", -1) // Добавлено получение userId

        val destination = if (isLoggedIn && userId != -1) { // Проверяем и isLoggedIn и userId
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, destination))
        finish()
    }
}

