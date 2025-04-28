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
//        copyDatabaseFromAssets(this)

        ApiManager.init(this)

        val destination = if (UserSession.isLoggedIn(this)) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, destination))
        finish()
    }
}



