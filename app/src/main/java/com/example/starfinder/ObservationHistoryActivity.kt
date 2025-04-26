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
import com.example.starfinder.databinding.ActivityBaseBinding
import com.example.starfinder.databinding.HistoryBinding
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.models.Observation
import com.example.starfinder.services.DataService
import java.text.SimpleDateFormat
import java.util.Locale

class ObservationHistoryActivity : BaseActivity() {
    private lateinit var binding: HistoryBinding // Используйте правильное имя Binding
    private lateinit var dataService: DataService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = HistoryBinding.inflate(layoutInflater)
        baseBinding.contentFrame.addView(binding.root)

        setToolbarTitle("История наблюдений") // Можно установить заголовок

        dataService = DataService(applicationContext).also {
            if (!it.checkDatabase()) {
                Toast.makeText(this, "Ошибка доступа к базе данных", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
        testDatabaseContents()

        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("UserId", -1)

        if (userId == -1) {
            showError("Требуется авторизация")
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 2000)
            return
        }

        loadObservations(userId)


    }


    private fun loadObservations(userId: Int) {
        try {
            Log.d("DEBUG", "Загружаем наблюдения для userId = $userId")
            val observations = dataService.getWithQuery("SELECT * FROM Observation WHERE UserId = ?", arrayOf(userId.toString())) {
                    cursor ->
                Observation(
                    observationId = cursor.getInt(cursor.getColumnIndexOrThrow("ObservationId")),
                    observationDateTime = cursor.getString(cursor.getColumnIndexOrThrow("ObservationDateTime")),
                    observationLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow("ObservationLatitude")),
                    observationLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow("ObservationLongitude")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("UserId"))
                )
            }
            Log.d("DEBUG", "Количество загруженных наблюдений: ${observations.size}")
            if (observations.isEmpty()) {
                showEmptyState()
            } else {
                setupRecyclerView(observations)
                binding.emptyHistoryView.visibility = View.GONE
                binding.observationHistoryRecyclerView.visibility = View.VISIBLE
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
                "SELECT * FROM Observation", // без WHERE!
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
        binding.observationHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ObservationHistoryActivity)
            adapter = ObservationAdapter(observations, dataService)
        }
    }

    private fun showEmptyState() {
        binding.emptyHistoryView.text = "История наблюдений пуста"
        binding.emptyHistoryView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.emptyHistoryView.text = message
        binding.emptyHistoryView.visibility = View.VISIBLE
        binding.observationHistoryRecyclerView.visibility = View.GONE
    }


    private fun showObservationDetails(observation: Observation) {
        // Получаем связанные небесные тела для этого наблюдения
        val celestialBodies =
            dataService.getCelestialBodiesForObservation(observation.observationId)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Детали наблюдения")
            .setMessage(buildDetailsMessage(observation, celestialBodies))
            .setPositiveButton("OK", null)
            .create()
        dialog.show()
    }

    private fun buildDetailsMessage(
        observation: Observation,
        celestialBodies: List<CelestialBody>
    ): String {
        return """
            ${formatDateTime(observation.observationDateTime)}
            ${observation.observationLatitude?.toString() ?: "N/A"}, 
                      ${observation.observationLongitude?.toString() ?: "N/A"}
            
            Наблюдаемый объекты:
            ${celestialBodies.joinToString("\n") { "• ${it.celestialBodyName}" }}
        """.trimIndent()
    }

    private fun formatDateTime(dateTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateTime)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateTime
        }


    }
}
