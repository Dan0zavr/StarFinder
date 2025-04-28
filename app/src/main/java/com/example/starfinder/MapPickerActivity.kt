package com.example.starfinder

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView

class MapPickerActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var selectedLocation: Point? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация MapKit ДО setContentView
        MapKitFactory.setApiKey(BuildConfig.MAPS_API_KEY)
        MapKitFactory.initialize(this)

        setContentView(R.layout.activity_map_picker)

        // После setContentView можно искать View
        mapView = findViewById(R.id.mapview)
        val map = mapView.mapWindow.map

        // Получаем координаты из интента
        val initialLat = intent.getDoubleExtra("current_lat", 55.751574)
        val initialLon = intent.getDoubleExtra("current_lon", 37.573856)

        // Перемещаем камеру на начальные координаты
        map.move(
            CameraPosition(
                Point(initialLat, initialLon),
                14.0f, 0.0f, 0.0f
            ),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )

        // Обработка нажатий по карте
        map.addInputListener(object : InputListener {
            override fun onMapTap(map: Map, point: Point) {
                selectedLocation = point
                map.mapObjects.clear()
                map.mapObjects.addPlacemark(point) // Ставим метку на место тапа
            }

            override fun onMapLongTap(map: Map, point: Point) {}
        })

        // Кнопка сохранения
        findViewById<Button>(R.id.btnSaveLocation).setOnClickListener {
            selectedLocation?.let { point ->
                setResult(RESULT_OK, Intent().apply {
                    putExtra("latitude", point.latitude)
                    putExtra("longitude", point.longitude)
                })
                finish()
            } ?: Toast.makeText(this, "Сначала выберите место на карте", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}