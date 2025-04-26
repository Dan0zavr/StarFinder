package com.example.starfinder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.services.AstronomicCalculations
import com.example.starfinder.services.CompassService
import com.example.starfinder.services.CoordinateService
import com.example.starfinder.viewmodels.MainViewModel
import com.example.starfinder.viewmodels.factories.MainViewModelFactory
import java.util.*
import kotlin.math.*

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var overlayView: OverlayView

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            initEverything()
        } else {
            Toast.makeText(
                this,
                "Permissions denied - some features may not work",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val starSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getParcelableExtra<CelestialBody>("selected_star")?.let { star ->
                viewModel.selectStar(star)
                Toast.makeText(this, "Selected: ${star.celestialBodyName}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Используем layout из BaseActivity, добавляем контент
        layoutInflater.inflate(R.layout.sample_star_find, findViewById(R.id.contentFrame), true)

        overlayView = findViewById(R.id.overlayView)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(requiredPermissions)
        } else {
            initEverything()
        }
    }

    private fun initEverything() {
        checkGpsEnabled()
        startCamera()
        initViewModel()
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkGpsEnabled() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable GPS for accurate results", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewView).surfaceProvider)
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Camera initialization failed", e)
                Toast.makeText(
                    this,
                    "Camera initialization failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun initViewModel() {
        val factory = MainViewModelFactory(
            CoordinateService(this),
            CompassService(this)
        )
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setupObservers()
        viewModel.start()
    }

    private fun setupObservers() {
        viewModel.location.observe(this) { location ->
            findViewById<TextView>(R.id.latitude).text =
                location?.let { "Lat: ${"%.6f".format(it.latitude)}" } ?: "Lat: Unknown"
            findViewById<TextView>(R.id.longitude).text =
                location?.let { "Lon: ${"%.6f".format(it.longitude)}" } ?: "Lon: Unknown"
            updateStarOverlay()
        }

        viewModel.azimuth.observe(this) { azimuth ->
            findViewById<TextView>(R.id.azimut).text = "Azimuth: ${"%.1f".format(azimuth)}°"
            updateStarOverlay()
        }

        viewModel.pitch.observe(this) { pitch ->
            findViewById<TextView>(R.id.nakl).text = "Altitude: ${"%.1f".format(pitch)}°"
            updateStarOverlay()
        }

            findViewById<TextView>(R.id.starInfo).setOnClickListener {
                val intent = Intent(this, StarSelectionActivity::class.java).apply {
                    putExtra("current_latitude", viewModel.location.value?.latitude?.toDouble() ?: 0.0)
                    putExtra("current_longitude", viewModel.location.value?.longitude?.toDouble() ?: 0.0)

                    // Получаем ID пользователя из SharedPreferences
                    val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    putExtra("user_id", sharedPref.getInt("user_id", 0))
                }
                starSelectionLauncher.launch(intent)
        }
    }

    private fun updateStarOverlay() {
        val star = viewModel.selectedStar.value ?: run {
            overlayView.updatePosition(0.0, 0.0, false)
            return
        }

        val location = viewModel.location.value ?: run {
            overlayView.updatePosition(0.0, 0.0, false)
            return
        }

        val currentAzimuth = viewModel.azimuth.value?.toDouble() ?: 0.0
        val currentAltitude = viewModel.pitch.value?.toDouble() ?: 0.0

        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        val (starAzimuth, starAltitude) = AstronomicCalculations().equatorialToHorizontal(
            raDeg = star.ascension.toDouble(),
            decDeg = star.deflection.toDouble(),
            latDeg = location.latitude,
            lonDeg = location.longitude,
            timeUtc = now
        )

        Log.d("DEBUG", "${starAzimuth}, ${starAltitude}")


        // Нормализация углов
        val azDiff = normalizeAngle(starAzimuth - currentAzimuth)
        val altDiff = (starAltitude - currentAltitude).coerceIn(-90.0, 90.0)

        // Определение видимости
        val isVisible = abs(azDiff) < 10.0 && abs(altDiff) < 10.0

        // Преобразование в экранные координаты
        val screenX = (azDiff / 90.0).coerceIn(-1.0, 1.0)
        val screenY = (altDiff / 90.0).coerceIn(-1.0, 1.0)

        overlayView.updatePosition(screenX, screenY, isVisible, star.celestialBodyName)
    }

    private fun normalizeAngle(degrees: Double): Double {
        return ((degrees % 360.0) + 540.0) % 360.0 - 180.0 // Нормализация в [-180, 180]
    }


    override fun onStart() {
        super.onStart()
        if (allPermissionsGranted()) {
            viewModel.start()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stop()
    }
}
