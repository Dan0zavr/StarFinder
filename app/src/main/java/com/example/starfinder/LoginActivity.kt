package com.example.starfinder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.starfinder.services.DataService
import com.example.starfinder.services.SharedPreferences.SessionManager
import com.example.starfinder.viewmodels.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_in_window)

        val dbHelper = DataService(this)
        viewModel = AuthViewModel(dbHelper)

        val emailEdit = findViewById<EditText>(R.id.editTextTextEmailAddress2)
        val passEdit = findViewById<EditText>(R.id.editTextTextPassword3)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val registerBtn = findViewById<Button>(R.id.registerButton)

        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                Toast.makeText(this, "Добро пожаловать, ${user.userName}!", Toast.LENGTH_SHORT).show()
                // Сохраняем информацию о том, что пользователь вошел
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.apply()

                // Переходим на экран планирования
                startActivity(Intent(this, PlanningActivity::class.java))
                finish() // Закрываем окно входа
            } else {
                Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show()
            }
        }

        loginBtn.setOnClickListener {
            val email = emailEdit.text.toString()
            val password = passEdit.text.toString()
            viewModel.login(email, password)
        }

        registerBtn.setOnClickListener {
            // Переход к окну регистрации
            val intent = Intent(this, RegisterActivity::class.java)  // Здесь указываешь активность для регистрации
            startActivity(intent)
        }
    }
}
