package com.example.starfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Загружаем основной макет с Drawer и Toolbar
        setContentView(R.layout.activity_base)

        // Настройка Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Настройка DrawerToggle
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Обработка NavigationView
        findViewById<NavigationView>(R.id.navigationView)
            .setNavigationItemSelectedListener { item ->
                Toast.makeText(this, "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
                when (item.itemId) {
                    R.id.observe  -> startActivity(Intent(this, MainActivity::class.java))
                    R.id.planning -> startActivity(Intent(this, ObservationPlanningActivity::class.java))
                    R.id.history  -> startActivity(Intent(this, ObservationHistoryActivity::class.java))
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
    }
}
