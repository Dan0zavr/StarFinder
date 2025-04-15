package com.example.starfinder.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class CompassService(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

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

            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // orientation[0] = азимут (в радианах), переведём в градусы
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()

            // Убедимся, что угол в пределах 0–360
            if (azimuth < 0) {
                azimuth += 360f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Не используется сейчас
    }
}
