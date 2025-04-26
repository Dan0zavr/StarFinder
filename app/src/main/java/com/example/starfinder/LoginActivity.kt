package com.example.starfinder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.databinding.LogInWindowBinding
import com.example.starfinder.models.User
import com.example.starfinder.services.DataService
import com.example.starfinder.services.SharedPreferences.SessionManager
import com.example.starfinder.viewmodels.AuthViewModel
import com.example.starfinder.viewmodels.factories.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LogInWindowBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LogInWindowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this,
            AuthViewModelFactory(DataService(this)))[AuthViewModel::class.java]

        setupObservers()
        setupListeners()
    }


    private fun setupObservers() {
        viewModel.loginResult.observe(this) { user ->
            user?.let {
                saveUserSession(it)
                navigateToMainScreen(it)
            } ?: showLoginError()
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.editTextTextEmailAddress2.text.toString()
            val password = binding.editTextTextPassword3.text.toString()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.registerButton.setOnClickListener {
            navigateToRegisterScreen()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextTextEmailAddress2.error = "Введите корректный email"
            return false
        }
        if (password.isEmpty() || password.length < 6) {
            binding.editTextTextPassword3.error = "Пароль должен содержать минимум 6 символов"
            return false
        }
        return true
    }

    private fun saveUserSession(user: User) {
        getSharedPreferences("user_prefs", MODE_PRIVATE).edit().apply {
            putInt("UserId", user.userId) // user - объект из БД
            apply()
        }
    }

    private fun navigateToMainScreen(user: User) {
        startActivity(Intent(this, MainActivity::class.java))
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("UserId", user.userId) // user - объект с сервера/БД
            putBoolean("isLoggedIn", true)
            apply()
        }
        finishAffinity()
    }

    private fun showLoginError() {
        Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRegisterScreen() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}