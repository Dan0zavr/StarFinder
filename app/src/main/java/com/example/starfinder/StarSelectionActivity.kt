package com.example.starfinder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.databinding.ActivityStarSelectionBinding
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.models.CelestialBodyInObservation
import com.example.starfinder.models.Observation
import com.example.starfinder.models.StarInfo
import com.example.starfinder.services.Api.ApiManager
import com.example.starfinder.services.DataService
import com.example.starfinder.viewmodels.StarSearchViewModel
import com.example.starfinder.viewmodels.factories.StarSearchViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StarSelectionActivity : AppCompatActivity() {

    private lateinit var viewModel: StarSearchViewModel
    private lateinit var dataService: DataService
    private var _binding: ActivityStarSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStarSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ApiManager.init(applicationContext)

        dataService = DataService(applicationContext)

        setupUI()
        initViewModel()
    }

    private fun setupUI() {
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()

            if (query.isNotBlank()) {
                viewModel.searchStar(query)
            }

        }
    }

    private fun initViewModel(){
        val factory = StarSearchViewModelFactory(dataService)
        viewModel = ViewModelProvider(this, factory)[StarSearchViewModel::class.java]
        setupObservers()
    }

    private fun setupObservers() {

        viewModel.starResults.observe(this) { stars ->
            val adapter = StarAdapter(this, stars).apply {
                setOnItemClickListener { star ->
                    binding.searchEditText.setText(star.name)
                    processStarSelection(star)

                }
            }
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

    private fun processStarSelection(starInfo: StarInfo) {

        // Получаем данные из MainActivity через Intent
        val latitude = intent.getDoubleExtra("current_latitude", 0.0)
        val longitude = intent.getDoubleExtra("current_longitude", 0.0)
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Ошибка: координаты не переданы", Toast.LENGTH_SHORT).show()
        }

        var insertedStar = -1L
        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("UserId", -1)
        lateinit var star: CelestialBody

        //Добавляем или устанавливаем звезду
        if(viewModel.starId == -1L){
            star = CelestialBody(
                celestialBodyId = null,
                celestialBodyName = starInfo.name,
                deflection = starInfo.dec,
                ascension = starInfo.ra,
                dataSourceId = starInfo.dataSourceId
            )

            insertedStar = dataService.insert("CelestialBody", star)
        }
        else{
            insertedStar = viewModel.starId
            star = dataService.getWithQuery("SELECT * FROM CelestialBody WHERE CelestialBodyId = ?", arrayOf(insertedStar.toString())){
                    cursor ->
                CelestialBody(
                    celestialBodyId = cursor.getInt(cursor.getColumnIndexOrThrow("CelestialBodyId")),
                    celestialBodyName = cursor.getString(cursor.getColumnIndexOrThrow("CelestialBodyName")),
                    ascension = cursor.getDouble(cursor.getColumnIndexOrThrow("Ascension")),
                    deflection = cursor.getDouble(cursor.getColumnIndexOrThrow("Deflection")),
                    dataSourceId = cursor.getInt(cursor.getColumnIndexOrThrow("DataSourceId"))

                )
            }.first()
        }

        //Если нет ошибок, создаем наблюдение и связываем
        if (insertedStar != -1L) {
            val insertedObservation = createObservation(latitude, longitude, userId)
            createLink(insertedStar, insertedObservation)
            setResult(RESULT_OK, Intent().apply {
                putExtra("selected_star", star)
            })


            finish()
        } else {
            Toast.makeText(
                this,
                "Ошибка при сохранении наблюдения",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun createObservation(latitude: Double, longitude: Double, userId: Int): Long{
        val observation = Observation(
            observationId = null,
            observationLatitude = latitude,
            observationLongitude = longitude,
            observationDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
            userId = userId
        )
        val insertedObservation = dataService.insert("Observation", observation)

        Log.d("OBSERVATION", "Creating new observation for user $userId")
        return insertedObservation
    }

    fun createLink(insertedStar: Long, insertedObservation: Long){
        val link = CelestialBodyInObservation(
            celestialBodyId = insertedStar.toInt(),
            observationId = insertedObservation.toInt()
        )
        dataService.insert("CelestialBodyInObservation", link)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}


