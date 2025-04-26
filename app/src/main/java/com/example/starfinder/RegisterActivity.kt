package com.example.starfinder

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.starfinder.R
import com.example.starfinder.databinding.RegisterWindowBinding
import com.example.starfinder.services.DataService
import com.example.starfinder.viewmodels.AuthViewModel
import com.example.starfinder.viewmodels.factories.AuthViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: RegisterWindowBinding
    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory(DataService(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterWindowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextUserName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (validateInput(username, email, password)) {
                viewModel.register(username, email, password) { success ->
                    if (success != -1L) {
                        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        binding.editTextEmail.error = "Пользователь с таким email уже существует"
                    }
                }
            }
        }

        binding.buttonGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(username: String, email: String, password: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.editTextUserName.error = "Введите имя пользователя"
            isValid = false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Введите корректный email"
            isValid = false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.editTextPassword.error = "Пароль должен содержать минимум 6 символов"
            isValid = false
        }

        return isValid
    }
}