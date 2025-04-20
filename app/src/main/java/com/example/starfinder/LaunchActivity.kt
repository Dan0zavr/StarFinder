package com.example.starfinder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.starfinder.services.SharedPreferences.SessionManager

class LaunchActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = SessionManager.getUserEmail(this)

        if (email != null) {
            // Пользователь уже вошел — открываем окно планирования наблюдений
            startActivity(Intent(this, PlanningActivity::class.java))
        } else {
            // Пользователь не вошел — открываем окно входа
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Закрыть MainActivity, чтобы не возвращаться к нему по back
    }
}