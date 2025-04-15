package com.example.starfinder

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.services.CoordinateService
import com.example.starfinder.viewmodels.MainViewModel
import com.example.starfinder.viewmodels.factories.MainViewModelFactory
//private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sample_star_find)

        // 1. Создаем сервис координат с контекстом Activity
        val coordinateService = CoordinateService(this)

        // 2. Создаем ViewModel через фабрику
        val factory = MainViewModelFactory(coordinateService)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        // 3. Подписываемся на координаты
        viewModel.location.observe(this) { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                // Используй координаты
                Log.d("MainActivity", "Lat: $lat, Lon: $lon")
            }
        }

        // 4. Запрашиваем координаты
        viewModel.fetchCurrentLocation()

//        viewModel.location.observe(this) { location ->
//            if (location != null) {
//                binding.textViewLatitude.text = "Latitude: ${location.latitude}"
//                binding.textViewLongitude.text = "Longitude: ${location.longitude}"
//            } else {
//                binding.textViewLatitude.text = "Не удалось получить координаты"
//            }
//        }
    }
}