package com.example.starfinder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.databinding.ActivityStarSelectionBinding
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.services.Api.ApiManager
import com.example.starfinder.services.DataService
import com.example.starfinder.viewmodels.StarSearchViewModel

class StarSelectionActivity : AppCompatActivity() {
    private lateinit var viewModel: StarSearchViewModel
    private var _binding: ActivityStarSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStarSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ApiManager.init(applicationContext)
        viewModel = ViewModelProvider(this)[StarSearchViewModel::class.java]

        setupUI()
        setupObservers()

    }

    private fun setupUI() {
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotBlank()) {
                viewModel.searchStarByName(query)  // Важно: вызываем нужный метод
            }
        }

        binding.starListView.setOnItemClickListener { _, _, position, _ ->
            // Получаем выбранное небесное тело из результатов поиска
            viewModel.starResults.value?.get(position)?.let { selectedStar ->

                // Создаем объект CelestialBody для вставки в базу данных
                val celestialBody = CelestialBody(
                    celestialBodyId = 0, // Значение по умолчанию для автогенерации ID
                    celestialBodyName = selectedStar.name,
                    typeId = 1,  // Пример типа, вам нужно указать правильный ID для типа
                    deflection = selectedStar.dec,
                    ascension = selectedStar.ra,
                    dataSourceId = 1 // Пример идентификатора источника данных, укажите нужное значение
                )

                // Вставляем в базу данных
                val dataService = DataService(this)
                val isInserted = dataService.insertCelestialBody(celestialBody)

                if (isInserted) {
                    // Если вставка успешна, создаем Intent для передачи данных в предыдущую активность
                    val intent = Intent().apply {
                        putExtra("selected_star_name", selectedStar.name)
                        putExtra("selected_star_ra", selectedStar.ra)
                        putExtra("selected_star_dec", selectedStar.dec)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    // Если вставка не удалась, показываем ошибку
                    Toast.makeText(this, "Ошибка при добавлении небесного тела в базу данных", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.starResults.observe(this) { stars ->
            val adapter = StarAdapter(this, stars)
            binding.starListView.adapter = adapter
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}




