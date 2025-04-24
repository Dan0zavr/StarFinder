package com.example.starfinder.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class CompassService(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    var onAzimuthPitchChanged: ((Float, Float) -> Unit)? = null

    var azimuth: Float = 0f  // Направление на север (в градусах)
        private set

    var pitch: Float = 0f    // Наклон вверх/вниз (в градусах)
        private set

    var roll: Float = 0f
        private set

    fun start() {
        gravitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }


    private val gravity = FloatArray(3)

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_GRAVITY -> {
                System.arraycopy(event.values, 0, gravity, 0, 3)
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                if (gravity.sum() == 0f) return

                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                // Получаем сырой pitch (-90°..90°)
                val rawPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                val rawRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()

                val isUpsideDown = abs(rawRoll) > 135f

                // Корректируем pitch с учетом переворота
                val correctedPitch = when {
                    isUpsideDown -> -rawPitch
                    else -> rawPitch
                }

                // Определяем направление камеры
                val isCameraUp = gravity[2] > 0.5f

                // Инвертируем pitch для астрономической системы
                val grAltitude = when {
                    isCameraUp -> -90f - rawPitch  // Камера вверх: 90° → 0° → -90°
                    else -> 90f + rawPitch       // Камера вниз: -90° → 0° → 90°
                }

                var altitude = 0.toFloat()


                if(grAltitude < -90) {
                    val diff = 90 + grAltitude

                    if (diff < 0) {
                        altitude = -(90 + diff)
                    }
                    else{
                        altitude = grAltitude
                    }
                }
                else if(grAltitude > 90){
                    var diff = 90 - grAltitude

                    if(diff < 0){
                        altitude = 90 + diff
                    }
                    else{
                        altitude = grAltitude
                    }
                }
                else{
                    altitude = grAltitude
                }


                onAzimuthPitchChanged?.invoke(azimuth, altitude)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Не используется сейчас
    }

    fun calculateAzimuth(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = Math.sin(deltaLambda) * Math.cos(phi2)
        val x = Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLambda)
        val azimuth = Math.atan2(y, x)
        return Math.toDegrees(azimuth).toFloat()
    }
}
