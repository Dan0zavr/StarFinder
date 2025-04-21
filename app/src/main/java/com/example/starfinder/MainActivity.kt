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
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import android.Manifest
import android.content.Intent
import android.location.LocationManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.starfinder.services.CompassService
import java.io.FileOutputStream

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )
    private val REQUEST_CODE_PERMISSIONS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.sample_star_find, findViewById(R.id.contentFrame), true)

        // Проверка разрешений
        if (!allPermissionsGranted()) {
            requestPermissions()
        } else {
            initEverything()
        }
    }

    private fun initEverything() {
        startCamera()

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Включите GPS", Toast.LENGTH_SHORT).show()
        }

        // Добавляем overlay поверх камеры
        val overlay = OverlayView(this)
        addContentView(overlay, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        // Инициализация ViewModel
        val compassService = CompassService(this)
        val coordinateService = CoordinateService(this)
        val factory = MainViewModelFactory(coordinateService, compassService)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        observeViewModel()
        viewModel.start()
    }

    private fun observeViewModel() {
        viewModel.location.observe(this) { location ->
            findViewById<TextView>(R.id.latitude).text =
                location?.let { "Широта: ${it.latitude}" } ?: "Широта: Не удалось получить"
            findViewById<TextView>(R.id.longitude).text =
                location?.let { "Долгота: ${it.longitude}" } ?: "Долгота: Не удалось получить"
        }

        viewModel.azimuth.observe(this) { azimuth ->
            findViewById<TextView>(R.id.azimut).text = "Азимут: ${"%.1f".format(azimuth)}°"
        }

        viewModel.pitch.observe(this) { pitch ->
            findViewById<TextView>(R.id.nakl).text = "Высота: ${"%.1f".format(pitch)}°"
        }
    }

    override fun onStart() {
        super.onStart()
        if (allPermissionsGranted()) {
            startCamera()
            viewModel.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stop()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewView).surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            } catch (e: Exception) {
                Log.e("CameraX", "Ошибка запуска камеры", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initEverything()
            } else {
                Toast.makeText(this, "Необходимо разрешение для работы", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


