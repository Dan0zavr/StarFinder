package com.example.starfinder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.starfinder.viewmodels.ObservationPlanViewModel
import java.util.Calendar
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.databinding.PlanningWindowBinding
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.models.CelestialBodyInObservation
import com.example.starfinder.models.Observation
import com.example.starfinder.services.DataService
import com.example.starfinder.viewmodels.factories.ObservationPlanViewModelFactory

class ObservationPlanningActivity : BaseActivity() {

    private lateinit var viewModel: ObservationPlanViewModel
    private lateinit var dataService: DataService
    private  lateinit var binding: PlanningWindowBinding

    // Убрали ListView и адаптер, так как они больше не нужны
    private val SELECT_STAR_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PlanningWindowBinding.inflate(layoutInflater)
        baseBinding.contentFrame.addView(binding.root)

        setToolbarTitle("Планирование")

        dataService = DataService(applicationContext)
        initViewModel()
        setupUI()
    }

    private fun initViewModel() {
        val factory = ObservationPlanViewModelFactory(dataService)
        viewModel = ViewModelProvider(this, factory)[ObservationPlanViewModel::class.java]
        setupObservers()
    }

    private fun setupUI() {
        // Устанавливаем слушатель клика на поле поиска
        binding.searchCelestialBody.setOnClickListener {
            openStarSelection()
        }

        // Делаем поле поиска нефокусируемым, чтобы не показывалась клавиатура
        binding.searchCelestialBody.isFocusable = false
        binding.searchCelestialBody.isClickable = true

        // Остальные обработчики без изменений
        binding.latitudeEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.setLatitude(binding.latitudeEdit.text.toString().toDoubleOrNull() ?: 0.0)
        }

        binding.longitudeEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.setLongitude(binding.longitudeEdit.text.toString().toDoubleOrNull() ?: 0.0)
        }

        binding.selectDateButton.setOnClickListener { showDatePicker() }
        binding.selectTimeButton.setOnClickListener { showTimePicker() }
        binding.saveObservationButton.setOnClickListener { saveObservation() }
    }

    private fun openStarSelection() {
        val intent = Intent(this, StarSelectionActivity::class.java)
        startActivityForResult(intent, SELECT_STAR_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_STAR_REQUEST && resultCode == RESULT_OK) {
            data?.getParcelableExtra<CelestialBody>("selected_star")?.let { star ->
                binding.searchCelestialBody.setText(star.celestialBodyName)
                viewModel.setCelestialBody(star) // Сохраняем в LiveData
            }
        }
    }

    private fun saveObservation() {

        viewModel.setLatitude(binding.latitudeEdit.text.toString().toDoubleOrNull() ?: 0.0)
        viewModel.setLongitude(binding.longitudeEdit.text.toString().toDoubleOrNull() ?: 0.0)

        // 1. Получаем звезду через LiveData
        val star = viewModel.selectedStar.value ?: run {
            Toast.makeText(this, "Выберите небесное тело", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Проверяем ID звезды
        val starId = star.celestialBodyId ?: run {
            Toast.makeText(this, "Ошибка: неверный ID звезды", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Создаём наблюдение в транзакции
        try {
            // 4. Создаём и сохраняем наблюдение
            val observation = Observation(
                observationId = null,
                observationDateTime = "${viewModel.getCurrentDate()}T${viewModel.getCurrentTime()}:00",
                observationLatitude = viewModel.getCurrentLatitude(),
                observationLongitude = viewModel.getCurrentLongitude(),
                userId = UserSession.getCurrentUserId(this)
            )

            val observationId = dataService.insert("Observation", observation)
            if (observationId == -1L) {
                Toast.makeText(this, "Ошибка сохранения наблюдения", Toast.LENGTH_SHORT).show()
                return
            }

            // 5. Создаём связь
            val link = CelestialBodyInObservation(
                celestialBodyId = starId,
                observationId = observationId.toInt()
            )

            if (dataService.insert("CelestialBodyInObservation", link) != -1L) {
                Toast.makeText(this, "Сохранено успешно", Toast.LENGTH_SHORT).show()

            }
        } catch (e: Exception) {
            Log.e("SAVE_ERROR", "Ошибка сохранения", e)
            Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
        }
    }

    // Остальные методы без изменений
    private fun setupObservers() {
        viewModel.date.observe(this) { date ->
            binding.selectedDateText.text = "Дата: $date"
        }

        viewModel.time.observe(this) { time ->
            binding.selectedTimeText.text = "Время: $time"
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d -> viewModel.setDate(y, m, d) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, h, m -> viewModel.setTime(h, m) },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

}


