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
import android.view.View
import com.example.starfinder.services.CompassService
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )
    private val REQUEST_CODE_PERMISSIONS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Сначала копируем базу данных
        copyDatabase(this)

        // Проверка, вошел ли пользователь
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            startActivity(Intent(this, PlanningActivity::class.java))
            finish()
            return  // <--- добавь это, чтобы прекратить дальнейшее выполнение
        } else {
            // Если не авторизован, открываем окно входа
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Закрываем MainActivity
        }


        val dbPath = this.getDatabasePath("StarFinder.db")
        Log.d("DB", "Exists: ${dbPath.exists()} | Path: $dbPath")

        // Включаем edge-to-edge и устанавливаем layout
        enableEdgeToEdge()
        setContentView(R.layout.sample_star_find)

        // Инициализируем ViewModel и сервисы
        val compassService = CompassService(this)
        initLocationServices()

        // Проверка и запрос разрешений
        if (!allPermissionsGranted()) {
            requestPermissions()
        } else {
            startCamera()
        }

        // Добавление Overlay поверх камеры
        val overlay = OverlayView(this)
        addContentView(
            overlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Обработка местоположения и азимута
        viewModel.location.observe(this) { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                val targetLat = 40.748817
                val targetLon = -73.985428
                val azimuth = compassService.calculateAzimuth(lat, lon, targetLat, targetLon)

                val angleDifference = Math.abs(azimuth - overlay.angle)
                overlay.angle = azimuth
                overlay.visibility = if (angleDifference < 5) View.GONE else View.VISIBLE
                overlay.invalidate()
            }
        }
    }

    private fun copyDatabase(context: Context) {
        val dbPath = context.getDatabasePath("StarFinder.db")
        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            try {
                context.assets.open("StarFinder.db").use { input ->
                    FileOutputStream(dbPath).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("DB", "База данных скопирована в: $dbPath")
            } catch (e: Exception) {
                Log.e("DB", "Ошибка при копировании базы данных", e)
            }
        } else {
            Log.d("DB", "База уже существует по пути: $dbPath")
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
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e("CameraX", "Ошибка запуска камеры", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun initLocationServices() {
        val coordinateService = CoordinateService(this)
        val factory = MainViewModelFactory(coordinateService)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

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
}
