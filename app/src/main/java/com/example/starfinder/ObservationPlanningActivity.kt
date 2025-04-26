package com.example.starfinder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.example.starfinder.viewmodels.ObservationPlanViewModel
import java.util.Calendar
import androidx.activity.viewModels
import com.example.starfinder.models.Observation
import com.example.starfinder.services.DataService

class ObservationPlanningActivity : BaseActivity() {

    private val viewModel: ObservationPlanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = findViewById<FrameLayout>(R.id.contentFrame)
        layoutInflater.inflate(R.layout.planning_window, container, true)

        val celestialEdit = findViewById<EditText>(R.id.searchCelestialBody)
        val coordinatesText = findViewById<TextView>(R.id.selectedCoordinatesText)
        val dateText = findViewById<TextView>(R.id.selectedDateText)
        val timeText = findViewById<TextView>(R.id.selectedTimeText)

        val selectCoordinatesButton = findViewById<Button>(R.id.selectCoordinatesButton)
        val selectDateButton = findViewById<Button>(R.id.selectDateButton)
        val selectTimeButton = findViewById<Button>(R.id.selectTimeButton)
        val saveButton = findViewById<Button>(R.id.saveObservationButton)

        val dataService = DataService(this)

        // Обновляем объект в ViewModel
        celestialEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.celestialBody.value = celestialEdit.text.toString()
            }
        }

        viewModel.latitude.observe(this) {
            coordinatesText.text = viewModel.getFormattedCoordinates()
        }
        viewModel.longitude.observe(this) {
            coordinatesText.text = viewModel.getFormattedCoordinates()
        }

        viewModel.date.observe(this) {
            dateText.text = viewModel.getFormattedDate()
        }

        viewModel.time.observe(this) {
            timeText.text = viewModel.getFormattedTime()
        }

        selectCoordinatesButton.setOnClickListener {
            viewModel.setCoordinates(55.75, 37.61) // Москва
        }

        selectDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d -> viewModel.setDate(y, m, d) },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        selectTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, h, m -> viewModel.setTime(h, m) },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        saveButton.setOnClickListener {
            val userId = getCurrentUserId()
            val date = viewModel.getFormattedDate()
            val time = viewModel.getFormattedTime()
            val lat = viewModel.latitude.value ?: 0.0
            val lon = viewModel.longitude.value ?: 0.0

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Выберите дату и время", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val observation = Observation(
                observationId = 0,
                observationDateTime = date,
                observationLatitude = lat,
                observationLongitude = lon,
                userId = userId
            )

            val success = dataService.insert("Observation", observation)
            if (success != -1L) {
                Log.d("ACCESSACCE", this.getDatabasePath("StarFinder.sqlite3").absolutePath)
                Toast.makeText(this, "Наблюдение сохранено", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("DB_PATH", this.getDatabasePath("StarFinder.sqlite3").absolutePath)
                Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentUserId(): Int {
        // Заглушка — здесь должен быть ID авторизованного пользователя
        return 1
    }
}

