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

        dataService = DataService(applicationContext)
        initViewModel()
        setupUI()
    }

    private fun setupUI() {
        binding.searchButton.setOnClickListener {
            viewModel.searchStar(binding.searchEditText.text.toString())
        }
    }

    private fun initViewModel() {
        val factory = StarSearchViewModelFactory(dataService)
        viewModel = ViewModelProvider(this, factory)[StarSearchViewModel::class.java]
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.starResults.observe(this) { stars ->
            val adapter = StarAdapter(this, stars).apply {
                setOnItemClickListener { star -> processStarSelection(star) }
            }
            binding.starListView.adapter = adapter
        }
    }

    private fun processStarSelection(starInfo: StarInfo) {
        viewModel.getOrCreateStar(starInfo) { celestialBody ->
            setResult(RESULT_OK, Intent().apply {
                putExtra("selected_star", celestialBody)  // Возвращаем CelestialBody
            })
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

