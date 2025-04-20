package com.example.starfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.starfinder.services.SharedPreferences.SessionManager


class PlanningActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.planning_window)

        // Пример кнопки выхода
//        val logoutButton = findViewById<Button>(R.id.logoutButton)
//        logoutButton.setOnClickListener {
//            SessionManager.clearSession(this)
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }
    }
}
