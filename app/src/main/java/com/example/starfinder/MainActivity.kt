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
import android.view.View
import com.example.starfinder.services.CompassService

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val compassService = CompassService(this)

        // Проверка разрешений
        if (!allPermissionsGranted()) {
            requestPermissions()
        } else {
            initLocationServices()
            startCamera()
        }

        // Инициализация оверлея
        val overlay = OverlayView(this)
        addContentView(
            overlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Включаем режим edge-to-edge
        enableEdgeToEdge()
        setContentView(R.layout.sample_star_find)

        // Запрашиваем координаты
        viewModel.fetchCurrentLocation()

        // Наблюдаем за изменениями местоположения
        viewModel.location.observe(this) { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                // Примерные координаты для небесного объекта
                val targetLat = 40.748817 // Примерная широта
                val targetLon = -73.985428 // Примерная долгота

                // Вычисляем азимут с помощью CompassService
                val azimuth = compassService.calculateAzimuth(lat, lon, targetLat, targetLon)

                // Обновляем угол стрелки
                overlay.angle = azimuth
                overlay.invalidate() // Перерисовываем стрелку

                // Скрываем стрелку, если разница в угле мала
                val angleDifference = Math.abs(azimuth - overlay.angle)
                overlay.visibility = if (angleDifference < 5) View.GONE else View.VISIBLE
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewView).surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Ошибка запуска камеры", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )

    private val REQUEST_CODE_PERMISSIONS = 10

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }

    private fun initLocationServices() {
        val coordinateService = CoordinateService(this)
        val factory = MainViewModelFactory(coordinateService)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        viewModel.location.observe(this) { location ->
            if (location != null) {
                Log.d("MainActivity", "Lat: ${location.latitude}, Lon: ${location.longitude}")
            }
        }

        viewModel.fetchCurrentLocation()
    }

}
