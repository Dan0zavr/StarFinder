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
                viewModel.searchStarByName(query)
            }
        }
    }

    private fun setupObservers() {
        viewModel.starResults.observe(this) { stars ->
            val adapter = StarAdapter(this, stars).apply {
                setOnItemClickListener { star ->
                    // При клике на звезду в списке:
                    // 1. Вставляем название в поле поиска
                    binding.searchEditText.setText(star.name)

                    // 2. Сразу создаем CelestialBody и передаем его
                    val celestialBody = CelestialBody(
                        celestialBodyId = 0,
                        celestialBodyName = star.name,
                        typeId = 1,
                        deflection = star.dec,
                        ascension = star.ra,
                        dataSourceId = 1
                    )

                    setResult(RESULT_OK, Intent().apply {
                        putExtra("selected_star", celestialBody)
                    })
                    finish()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}




