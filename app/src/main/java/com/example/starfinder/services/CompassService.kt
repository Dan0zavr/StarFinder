package com.example.starfinder.services

import android.R
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs

class CompassService(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    var onAzimuthPitchChanged: ((Float, Float) -> Unit)? = null

    var azimuth: Float = 0f  // Направление на север (в градусах)
        private set

    var pitch: Float = 0f    // Наклон вверх/вниз (в градусах)
        private set

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            // Преобразуем координаты: считаем, что камера направлена вперёд (вдоль оси Z)
            val remappedMatrix = FloatArray(9)
            SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_X,  // или AXIS_Y в зависимости от ориентации
                SensorManager.AXIS_Z,
                remappedMatrix
            )

            val orientation = FloatArray(3)
            SensorManager.getOrientation(remappedMatrix, orientation)

            val rawAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val rawPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()

            azimuth = (rawAzimuth + 360) % 360  // нормализуем [0, 360)
            pitch = -rawPitch                    // pitch без ручной коррекции

            Log.d("CurrentCoords", "${azimuth}, ${pitch}")
            onAzimuthPitchChanged?.invoke(azimuth, pitch)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Пока не используется
    }
}