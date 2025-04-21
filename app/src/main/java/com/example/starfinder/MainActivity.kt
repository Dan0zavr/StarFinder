package com.example.starfinder

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.services.CoordinateService
import com.example.starfinder.viewmodels.MainViewModel
import com.example.starfinder.viewmodels.factories.MainViewModelFactory
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.location.LocationManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.services.AstronomicCalculations
import com.example.starfinder.services.CompassService
import com.example.starfinder.services.DataService
import java.util.Calendar
import kotlin.math.abs

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var overlayView: OverlayView

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )
    private val REQUEST_CODE_PERMISSIONS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.sample_star_find, findViewById(R.id.contentFrame), true)

        overlayView = findViewById(R.id.overlayView)

        if (!allPermissionsGranted()) {
            requestPermissions()
        } else {
            initEverything()
        }
    }

    private fun initEverything() {
        startCamera()
        checkGpsEnabled()
        initViewModel()
        setupStarSelection()
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
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            } catch (e: Exception) {
                Log.e("CameraX", "Camera initialization failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun checkGpsEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViewModel() {
        val factory = MainViewModelFactory(
            CoordinateService(this),
            CompassService(this)
        )
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        viewModel.location.observe(this) { location ->
            findViewById<TextView>(R.id.latitude).text =
                location?.let { "Lat: ${it.latitude}" } ?: "Lat: Unknown"
            findViewById<TextView>(R.id.longitude).text =
                location?.let { "Lon: ${it.longitude}" } ?: "Lon: Unknown"
            updateArrowPosition()
        }

        viewModel.azimuth.observe(this) { azimuth ->
            findViewById<TextView>(R.id.azimut).text = "Azimuth: ${"%.1f".format(azimuth)}°"
            updateArrowPosition()
        }

        viewModel.pitch.observe(this) { pitch ->
            findViewById<TextView>(R.id.nakl).text = "Altitude: ${"%.1f".format(pitch)}°"
            updateArrowPosition()
        }

        viewModel.selectedStar.observe(this) { star ->
            star?.let {
                findViewById<TextView>(R.id.starInfo).text = "Selected: ${it.celestialBodyName}"
                updateArrowPosition()
            }
        }

        viewModel.start()
    }

    private fun setupStarSelection() {
        findViewById<TextView>(R.id.starInfo).setOnClickListener {
            startActivityForResult(
                Intent(this, StarSelectionActivity::class.java),
                STAR_SELECTION_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STAR_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            val star = data?.getParcelableExtra<CelestialBody>("selected_star")
            star?.let {
                viewModel.selectStar(it)
                findViewById<TextView>(R.id.starInfo).text = "Selected: ${it.celestialBodyName}"
                Log.d("StarTransfer", "Received star: ${it.celestialBodyName}")
            } ?: Log.e("StarTransfer", "Received null star")
        }
    }

    private fun updateArrowPosition() {
        Log.d("ArrowDebug", "Start update")
        val star = viewModel.selectedStar.value ?: run {
            Log.d("ArrowDebug", "No star selected")
            return
        }
        val location = viewModel.location.value ?: run {
            Log.d("ArrowDebug", "No location")
            return
        }
        val currentAzimuth = viewModel.azimuth.value ?: run {
            Log.d("ArrowDebug", "No azimuth")
            return
        }

        // Конвертируем координаты звезды
        val (starAzimuth, _) = AstronomicCalculations().equatorialToHorizontal(
            star.ascension.toDouble(),
            star.deflection.toDouble(),
            location.latitude,
            location.longitude,
            Calendar.getInstance()
        )

        // Вычисляем разницу между направлением на звезду и текущим азимутом
        var relAzimuth = starAzimuth - currentAzimuth
        // Нормализуем в диапазон [-180..180]
        relAzimuth = when {
            relAzimuth > 180 -> relAzimuth - 360
            relAzimuth < -180 -> relAzimuth + 360
            else -> relAzimuth
        }

        overlayView.updateDirection(relAzimuth.toFloat())

        Log.d("ArrowDebug", "StarAz: $starAzimuth, CurrAz: $currentAzimuth, RelAz: $relAzimuth")
    }

    private fun calculateRelativeDirection(currentAzimuth: Double, targetAzimuth: Double): Double {
        var diff = targetAzimuth - currentAzimuth
        diff = when {
            diff > 180 -> diff - 360
            diff < -180 -> diff + 360
            else -> diff
        }
        return (diff + 360) % 360  // Нормализация в диапазоне [0, 360]
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initEverything()
        }
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

    companion object {
        private const val STAR_SELECTION_REQUEST_CODE = 123
    }

}


