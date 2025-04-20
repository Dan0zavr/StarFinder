package com.example.starfinder

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.starfinder.R
import com.example.starfinder.services.DataService
import com.example.starfinder.viewmodels.AuthViewModel
import com.example.starfinder.viewmodels.factories.AuthViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(DataService(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_window)

        val usernameInput = findViewById<TextInputEditText>(R.id.editTextUserName)
        val emailInput = findViewById<TextInputEditText>(R.id.editTextEmail)
        val passwordInput = findViewById<TextInputEditText>(R.id.editTextPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegister)
        val goToLoginButton = findViewById<Button>(R.id.buttonGoToLogin)

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.register(username, email, password) { success ->
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Пользователь с таким email уже существует.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        goToLoginButton.setOnClickListener {
            finish()
        }
    }
}
