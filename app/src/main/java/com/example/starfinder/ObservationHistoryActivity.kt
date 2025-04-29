package com.example.starfinder

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.starfinder.databinding.HistoryBinding
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.models.Observation
import com.example.starfinder.services.DataService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObservationHistoryActivity : BaseActivity() {
    private lateinit var binding: HistoryBinding
    private lateinit var dataService: DataService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = HistoryBinding.inflate(layoutInflater)
        baseBinding.contentFrame.addView(binding.root)

        setToolbarTitle("История наблюдений")

        dataService = DataService(applicationContext).also {
            if (!it.checkDatabase()) {
                Toast.makeText(this, "Ошибка доступа к базе данных", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
        testDatabaseContents()

        val userId = getCurrentUserId()
        if (userId == -1) return

        loadObservations(userId)

    }

    private fun getCurrentUserId(): Int {
        return UserSession.getCurrentUserId(this).takeIf { it != -1 } ?: run {
            showError("Требуется авторизация")
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 2000)
            -1
        }
    }

    private fun loadObservations(userId: Int) {
        try {
            val currentDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(Date()) // Текущая дата и время

            Log.d("DEBUG", "Загружаем наблюдения для userId = $userId до $currentDateTime")

            val observations = dataService.getWithQuery(
                "SELECT * FROM Observation WHERE UserId = ? AND ObservationDateTime < ? ORDER BY ObservationDateTime DESC",
                arrayOf(userId.toString(), currentDateTime)
            ) { cursor ->
                Observation(
                    observationId = cursor.getInt(cursor.getColumnIndexOrThrow("ObservationId")),
                    observationDateTime = cursor.getString(cursor.getColumnIndexOrThrow("ObservationDateTime")),
                    observationLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow("ObservationLatitude")),
                    observationLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow("ObservationLongitude")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("UserId"))
                )
            }

            if (observations.isEmpty()) {
                showEmptyState()
            } else {
                setupRecyclerView(observations)
                binding.emptyHistoryView.visibility = View.GONE
                binding.historyRecyclerView.visibility = View.VISIBLE
                Log.d("DEBUG", "Загружено")
            }

        } catch (e: Exception) {
            Log.e("LOAD_ERROR", "Error loading observations", e)
            showError("Ошибка загрузки данных")
        }
    }

    private fun testDatabaseContents() {
        try {
            val allObservations = dataService.getWithQuery(
                "SELECT * FROM Observation",
                emptyArray()
            ) { cursor ->
                Observation(
                    observationId = cursor.getInt(cursor.getColumnIndexOrThrow("ObservationId")),
                    observationDateTime = cursor.getString(cursor.getColumnIndexOrThrow("ObservationDateTime")),
                    observationLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow("ObservationLatitude")),
                    observationLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow("ObservationLongitude")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("UserId"))
                )
            }

            Log.d("DEBUG", "ВСЕ НАБЛЮДЕНИЯ В БАЗЕ (${allObservations.size} штук):")
            allObservations.forEach {
                Log.d("DEBUG", "ID: ${it.observationId}, UserId: ${it.userId}, Дата: ${it.observationDateTime}")
            }
        } catch (e: Exception) {
            Log.e("DB_TEST", "Ошибка при чтении базы", e)
        }
    }

    private fun setupRecyclerView(observations: List<Observation>) {
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ObservationHistoryActivity) // Важно!
            adapter = ObservationAdapter(
                items = observations,
                dataService = dataService,
                itemLayoutRes = R.layout.item_observation
            )
        }
    }

    private fun showEmptyState() {
        binding.emptyHistoryView.text = "История наблюдений пуста"
        binding.emptyHistoryView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.emptyHistoryView.text = message
        binding.emptyHistoryView.visibility = View.VISIBLE
        binding.historyRecyclerView.visibility = View.GONE
    }

}
