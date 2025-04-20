package com.example.starfinder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.starfinder.services.SharedPreferences.SessionManager

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем, авторизован ли пользователь
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("isLoggedIn", false)

        // Определяем куда переходить
        val destinationClass = if (isLoggedIn) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        // Запускаем нужную активность и закрываем текущую
        startActivity(Intent(this, destinationClass))
        finish()
    }
}