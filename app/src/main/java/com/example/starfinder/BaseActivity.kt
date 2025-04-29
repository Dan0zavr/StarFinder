package com.example.starfinder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.starfinder.databinding.ActivityBaseBinding

open class BaseActivity : AppCompatActivity() {
    protected lateinit var baseBinding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(baseBinding.root)

        setupToolbar()
        setupNavigationDrawer()
    }

    private fun setupToolbar() {
        setSupportActionBar(baseBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            baseBinding.drawerLayout,
            baseBinding.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        baseBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        baseBinding.navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem)
            true
        }
    }

    private fun handleNavigationItemSelected(item: MenuItem): Boolean {
        baseBinding.drawerLayout.closeDrawer(GravityCompat.START)

        return when (item.itemId) {
            R.id.observe -> navigateToActivity<MainActivity>()
            R.id.plans -> navigateToActivity<PlansActivity>()
            R.id.planning -> navigateToActivity<ObservationPlanningActivity>()
            R.id.history -> navigateToActivity<ObservationHistoryActivity>()
            R.id.logout -> {
                performLogout()
                true
            }
            else -> false
        }
    }

    private inline fun <reified T : Activity> navigateToActivity(): Boolean {
        if (this !is T) {
            startActivity(Intent(this, T::class.java))
            finish()
        }
        return true
    }

    private fun performLogout() {
        // Очистка данных сессии
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().apply {
            clear()
            apply()
        }

        // Переход на экран авторизации с очисткой стека
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    protected fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }
}
