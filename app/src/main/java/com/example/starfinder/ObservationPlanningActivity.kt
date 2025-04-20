package com.example.starfinder

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class ObservationPlanningActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // BaseActivity уже вызвал setContentView(activity_base)

        // Вдуваем макет планирования в контейнер внутри activity_base
        val container = findViewById<FrameLayout>(R.id.contentFrame)
        layoutInflater.inflate(R.layout.planning_window, container, true)

        // Теперь можно найти элементы внутри planning_window
        // val edit = findViewById<EditText>(R.id.searchCelestialBody)
    }
}